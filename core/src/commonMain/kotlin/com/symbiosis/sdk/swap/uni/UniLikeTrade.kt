package com.symbiosis.sdk.swap.uni

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.NativeToken
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.thisOrWrapped
import com.symbiosis.sdk.internal.kbignum.UINT256_MAX
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.network.contract.checkTokenAllowance
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.uni.UniLikeSwapRepository.CalculatedRoute
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.hex.HexString

sealed interface UniLikeTrade {
    val route: CalculatedRoute
    val networkClient: NetworkClient

    val fee: BigInt
    val priceImpact: Percentage

    val routerAddress: ContractAddress get() = networkClient.network.routerAddress
    val tokens: NetworkTokenPair get() = route.tokens
    val path: List<Erc20Token> get() = route.pools.map { pool -> pool.pair.first } +
            route.tokens.second.thisOrWrapped

    val callDataOffset: BigInt

    fun recalculateExactIn(amountIn: BigInt): ExactIn =
        route.exactIn(amountIn)

    fun recalculateExactOut(amountOut: BigInt): CalculatedRoute.ExactOutResult =
        route.exactOut(amountOut)

    suspend fun callData(
        slippageTolerance: Percentage,
        recipient: EthereumAddress,
        deadline: BigInt? = null
    ): HexString

    suspend fun execute(
        credentials: Credentials,
        slippageTolerance: Percentage,
        deadline: BigInt? = null,
        gasProvider: GasProvider? = null,
        recipient: EthereumAddress = credentials.address
    ): UniLikeSwapTransaction

    data class ExactIn(
        override val route: CalculatedRoute,
        override val networkClient: NetworkClient,
        override val fee: BigInt,
        override val priceImpact: Percentage,
        val amountIn: BigInt,
        val amountOutEstimated: BigInt
    ) : UniLikeTrade {

        private val rawPath = path.map { it.tokenAddress }

        val value: BigInt = when (tokens.first) {
            is Erc20Token -> 0.bi
            is NativeToken -> amountIn
        }

        fun amountOutMin(slippageTolerance: Percentage): BigInt =
            (amountOutEstimated.toBigNum() * (1.bn - slippageTolerance.fractionalValue)).toBigInt()

        // https://github.com/symbiosis-finance/js-sdk/blob/cef348e5ca7263a369f0f8f6cfcc9255021993e1/src/router.ts#L87-L113
        override val callDataOffset: BigInt =
            when (tokens.first) {
                is NativeToken -> 0
                is Erc20Token -> 36
            }.bi

        override suspend fun callData(
            slippageTolerance: Percentage,
            recipient: EthereumAddress,
            deadline: BigInt?
        ): HexString {
            return when (route.tokens.first) {
                is NativeToken -> networkClient.router.getSwapExactNativeForTokensCallData(
                    amountOutMin = amountOutMin(slippageTolerance),
                    path = rawPath,
                    recipient = recipient,
                    deadline = deadline
                )
                is Erc20Token -> when (route.tokens.second) {
                    is NativeToken -> networkClient.router::getSwapExactTokensForNativeCallData
                    is Erc20Token -> networkClient.router::getSwapExactTokensForTokensCallData
                }.invoke(
                    /* amountIn = */amountIn,
                    /* amountOutMin = */amountOutMin(slippageTolerance),
                    /* path = */rawPath,
                    /* deadline = */deadline,
                    /* recipient = */recipient
                )
            }
        }

        override suspend fun execute(
            credentials: Credentials,
            slippageTolerance: Percentage,
            deadline: BigInt?,
            gasProvider: GasProvider?,
            recipient: EthereumAddress
        ): UniLikeSwapTransaction {
            require(slippageTolerance >= 0.bn && slippageTolerance <= 1.bn)

            val function = when (route.tokens.first) {
                is NativeToken -> networkClient.router::swapExactNativeForTokens
                is Erc20Token -> when (route.tokens.second) {
                    is NativeToken -> networkClient.router::swapExactTokensForNative
                    is Erc20Token -> networkClient.router::swapExactTokensForTokens
                }
            }

            return function(
                /* credentials = */credentials,
                /* amountIn = */amountIn,
                /* amountOutMin = */amountOutMin(slippageTolerance),
                /* path = */rawPath,
                /* deadline = */deadline,
                /* gasProvider = */gasProvider,
                /* recipient = */recipient.bigInt
            ).let { hash -> UniLikeSwapTransaction(networkClient, hash) }
        }
    }

    data class ExactOut(
        override val route: CalculatedRoute,
        override val networkClient: NetworkClient,
        override val fee: BigInt,
        override val priceImpact: Percentage,
        val amountOut: BigInt,
        val amountInEstimated: BigInt
    ) : UniLikeTrade {

        // https://github.com/symbiosis-finance/js-sdk/blob/cef348e5ca7263a369f0f8f6cfcc9255021993e1/src/router.ts#L114-L135
        override val callDataOffset: BigInt =
            when (tokens.first) {
                is NativeToken -> 0
                is Erc20Token -> 68
            }.bi

        fun value(slippageTolerance: Percentage) = when (tokens.first) {
            is Erc20Token -> 0.bi
            is NativeToken -> amountInMax(slippageTolerance)
        }

        fun amountInMax(slippageTolerance: Percentage) =
            (amountInEstimated.toBigNum() * (1.bn + slippageTolerance.fractionalValue)).toBigInt()

        override suspend fun callData(slippageTolerance: Percentage, recipient: EthereumAddress, deadline: BigInt?): HexString {
            TODO("Not yet implemented, because was not need yet")
        }

        suspend fun isApproveRequired(walletAddress: WalletAddress): Boolean {
            val inputToken = tokens.first

            if (inputToken !is Erc20Token)
                return false

            return !networkClient
                .getTokenContract(inputToken.tokenAddress)
                .checkTokenAllowance(walletAddress, routerAddress, BigInt.UINT256_MAX)
        }

        suspend fun approveMaxIfRequired(
            credentials: Credentials,
            gasProvider: GasProvider? = null
        ) {
            val inputToken = tokens.first
            if (inputToken !is Erc20Token)
                return
            if (!isApproveRequired(credentials.address))
                return

            networkClient.getTokenContract(inputToken.tokenAddress)
                .approveMax(
                    credentials = credentials,
                    spender = routerAddress,
                    gasProvider = gasProvider
                )
        }

        override suspend fun execute(
            credentials: Credentials,
            slippageTolerance: Percentage,
            deadline: BigInt?,
            gasProvider: GasProvider?,
            recipient: EthereumAddress
        ): UniLikeSwapTransaction {
            require(slippageTolerance >= 0.bn && slippageTolerance <= 1.bn)

            approveMaxIfRequired(credentials, gasProvider)

            val function = when (route.tokens.first) {
                is NativeToken -> networkClient.router::swapNativeForExactTokens
                is Erc20Token -> when (route.tokens.second) {
                    is NativeToken -> networkClient.router::swapTokensForExactNative
                    is Erc20Token -> networkClient.router::swapTokensForExactTokens
                }
            }

            return function(
                /* credentials = */credentials,
                /* amountInMax = */amountInMax(slippageTolerance),
                /* amountOut = */amountOut,
                /* path = */path.map { it.tokenAddress },
                /* deadline = */deadline,
                /* gasProvider = */gasProvider,
                /* recipient = */recipient.bigInt
            ).let { hash -> UniLikeSwapTransaction(networkClient, hash) }
        }
    }
}
