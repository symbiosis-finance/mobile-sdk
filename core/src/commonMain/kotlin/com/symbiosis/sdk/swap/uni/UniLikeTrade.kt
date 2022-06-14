package com.symbiosis.sdk.swap.uni

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.DecimalsNativeToken
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.currency.thisOrWrapped
import com.symbiosis.sdk.internal.kbignum.UINT256_MAX
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.network.contract.checkTokenAllowance
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.uni.UniLikeSwapRepository.CalculatedRoute
import com.symbiosis.sdk.transaction.Web3SwapTransaction
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.hex.HexString

sealed interface UniLikeTrade {
    val route: CalculatedRoute
    val networkClient: NetworkClient

    val fee: TokenAmount
    val priceImpact: Percentage

    val routerAddress: ContractAddress get() = networkClient.network.routerAddress
    val tokens: NetworkTokenPair get() = route.tokens
    val path: List<DecimalsToken> get() = listOf(route.tokens.first) +
            route.pools.map { pool -> pool.pair.second }.dropLast(n = 1) +
            route.tokens.second

    val callDataOffset: BigInt

    fun recalculateExactIn(amountIn: TokenAmount) = recalculateExactIn(amountIn.raw)

    fun recalculateExactIn(amountIn: BigInt): ExactIn =
        route.exactIn(amountIn)

    fun recalculateExactOut(amountOut: TokenAmount) = recalculateExactOut(amountOut.raw)

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
    ): Web3SwapTransaction

    data class ExactIn(
        override val route: CalculatedRoute,
        override val networkClient: NetworkClient,
        override val fee: TokenAmount,
        override val priceImpact: Percentage,
        val amountIn: TokenAmount,
        val amountOutEstimated: TokenAmount
    ) : UniLikeTrade {

        private val rawPath = path.map { it.thisOrWrapped.tokenAddress }

        val value: BigInt = when (tokens.first) {
            is DecimalsErc20Token -> 0.bi
            is DecimalsNativeToken -> amountIn.raw
        }

        fun amountOutMin(slippageTolerance: Percentage): TokenAmount {
            val amountOutEstimatedInt = amountOutEstimated.raw
            val amountOutMinInt =
                (amountOutEstimatedInt.toBigNum() * (1.bn - slippageTolerance.fractionalValue)).toBigInt()

            return TokenAmount(amountOutMinInt, amountOutEstimated.token)
        }

        // https://github.com/symbiosis-finance/js-sdk/blob/cef348e5ca7263a369f0f8f6cfcc9255021993e1/src/router.ts#L87-L113
        override val callDataOffset: BigInt =
            when (tokens.first) {
                is DecimalsNativeToken -> 0
                is DecimalsErc20Token -> 36
            }.bi

        override suspend fun callData(
            slippageTolerance: Percentage,
            recipient: EthereumAddress,
            deadline: BigInt?
        ): HexString {
            return when (route.tokens.first) {
                is DecimalsNativeToken -> networkClient.router.getSwapExactNativeForTokensCallData(
                    amountOutMin = amountOutMin(slippageTolerance).raw,
                    path = rawPath,
                    recipient = recipient,
                    deadline = deadline
                )
                is DecimalsErc20Token -> when (route.tokens.second) {
                    is DecimalsNativeToken -> networkClient.router::getSwapExactTokensForNativeCallData
                    is DecimalsErc20Token -> networkClient.router::getSwapExactTokensForTokensCallData
                }.invoke(
                    /* amountIn = */amountIn.raw,
                    /* amountOutMin = */amountOutMin(slippageTolerance).raw,
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
        ): Web3SwapTransaction {
            require(slippageTolerance >= 0.bn && slippageTolerance <= 1.bn)

            val function = when (route.tokens.first) {
                is DecimalsNativeToken -> networkClient.router::swapExactNativeForTokens
                is DecimalsErc20Token -> when (route.tokens.second) {
                    is DecimalsNativeToken -> networkClient.router::swapExactTokensForNative
                    is DecimalsErc20Token -> networkClient.router::swapExactTokensForTokens
                }
            }

            return function(
                /* credentials = */credentials,
                /* amountIn = */amountIn.raw,
                /* amountOutMin = */amountOutMin(slippageTolerance).raw,
                /* path = */rawPath,
                /* deadline = */deadline,
                /* gasProvider = */gasProvider,
                /* recipient = */recipient.bigInt
            ).let { hash -> Web3SwapTransaction(networkClient, hash) }
        }
    }

    data class ExactOut(
        override val route: CalculatedRoute,
        override val networkClient: NetworkClient,
        override val fee: TokenAmount,
        override val priceImpact: Percentage,
        val amountOut: TokenAmount,
        val amountInEstimated: TokenAmount
    ) : UniLikeTrade {

        // https://github.com/symbiosis-finance/js-sdk/blob/cef348e5ca7263a369f0f8f6cfcc9255021993e1/src/router.ts#L114-L135
        override val callDataOffset: BigInt =
            when (tokens.first) {
                is DecimalsNativeToken -> 0
                is DecimalsErc20Token -> 68
            }.bi

        fun value(slippageTolerance: Percentage) = when (tokens.first) {
            is DecimalsErc20Token -> 0.bi
            is DecimalsNativeToken -> amountInMax(slippageTolerance).raw
        }

        fun amountInMax(slippageTolerance: Percentage): TokenAmount {
            val amountInEstimatedInt = amountInEstimated.raw
            val amountInMaxInt =
                (amountInEstimatedInt.toBigNum() * (1.bn + slippageTolerance.fractionalValue)).toBigInt()
            return TokenAmount(amountInMaxInt, amountInEstimated.token)
        }

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
        ): Web3SwapTransaction {
            require(slippageTolerance >= 0.bn && slippageTolerance <= 1.bn)

            approveMaxIfRequired(credentials, gasProvider)

            val function = when (route.tokens.first) {
                is DecimalsNativeToken -> networkClient.router::swapNativeForExactTokens
                is DecimalsErc20Token -> when (route.tokens.second) {
                    is DecimalsNativeToken -> networkClient.router::swapTokensForExactNative
                    is DecimalsErc20Token -> networkClient.router::swapTokensForExactTokens
                }
            }

            return function(
                /* credentials = */credentials,
                /* amountInMax = */amountInMax(slippageTolerance).raw,
                /* amountOut = */amountOut.raw,
                /* path = */path.map { it.thisOrWrapped.tokenAddress },
                /* deadline = */deadline,
                /* gasProvider = */gasProvider,
                /* recipient = */recipient.bigInt
            ).let { hash -> Web3SwapTransaction(networkClient, hash) }
        }
    }
}
