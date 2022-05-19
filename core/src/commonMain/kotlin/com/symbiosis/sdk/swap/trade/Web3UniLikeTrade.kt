package com.symbiosis.sdk.swap.trade

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.NativeToken
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.swap.uni.Web3UniLikeCalculatedRoute
import com.symbiosis.sdk.swap.uni.Web3UniLikePendingTransaction
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress

sealed class Web3UniLikeTrade : SwapTrade {
    abstract val route: Web3UniLikeCalculatedRoute
    abstract val networkClient: NetworkClient

    abstract suspend fun execute(
        credentials: Credentials,
        slippageTolerance: BigNum /* 0.0 .. 1.0 */,
        deadline: BigInt? = null,
        gasProvider: GasProvider? = null,
        recipient: EthereumAddress = credentials.address
    ): Web3UniLikePendingTransaction

    protected val path: List<ContractAddress> get() =
        route.pools.map { it.pair.first.tokenAddress } +
                route.pools.last().pair.second.tokenAddress



    class ExactIn(
        override val networkClient: NetworkClient,
        override val fee: BigInt,
        override val priceImpact: BigNum,
        override val amountIn: BigInt,
        override val amountOutEstimated: BigInt,
        override val route: Web3UniLikeCalculatedRoute
    ) : Web3UniLikeTrade(), SwapTrade.ExactIn {

        fun amountOutMin(slippageTolerance: BigNum): BigInt =
            (amountOutEstimated.toBigNum() * (1.bn - slippageTolerance)).toBigInt()

        override suspend fun execute(
            credentials: Credentials,
            slippageTolerance: BigNum,
            deadline: BigInt?,
            gasProvider: GasProvider?,
            recipient: EthereumAddress
        ): Web3UniLikePendingTransaction {
            require(slippageTolerance >= 0.bn && slippageTolerance <= 1.bn)

            val function = when (route.pair.first) {
                is NativeToken -> networkClient.router::swapExactNativeForTokens
                is Erc20Token -> when (route.pair.second) {
                    is NativeToken -> networkClient.router::swapExactTokensForNative
                    is Erc20Token -> networkClient.router::swapExactTokensForTokens
                }
            }

            return function(
                /* credentials = */credentials,
                /* amountIn = */amountIn,
                /* amountOutMin = */amountOutMin(slippageTolerance),
                /* path = */path,
                /* deadline = */deadline,
                /* gasProvider = */gasProvider,
                /* recipient = */recipient.bigInt
            ).let { hash -> Web3UniLikePendingTransaction(networkClient, hash) }
        }
    }

    class ExactOut(
        override val networkClient: NetworkClient,
        override val fee: BigInt,
        override val priceImpact: BigNum,
        override val amountOut: BigInt,
        override val amountInEstimated: BigInt,
        override val route: Web3UniLikeCalculatedRoute
    ) : Web3UniLikeTrade(), SwapTrade.ExactOut {

        fun amountInMax(slippageTolerance: BigNum) =
            (amountInEstimated.toBigNum() * (1.bn + slippageTolerance)).toBigInt()

        override suspend fun execute(
            credentials: Credentials,
            slippageTolerance: BigNum,
            deadline: BigInt?,
            gasProvider: GasProvider?,
            recipient: EthereumAddress
        ): Web3UniLikePendingTransaction {
            require(slippageTolerance >= 0.bn && slippageTolerance <= 1.bn)

            val function = when (route.pair.first) {
                is NativeToken -> networkClient.router::swapNativeForExactTokens
                is Erc20Token -> when (route.pair.second) {
                    is NativeToken -> networkClient.router::swapTokensForExactNative
                    is Erc20Token -> networkClient.router::swapTokensForExactTokens
                }
            }

            return function(
                /* credentials = */credentials,
                /* amountInMax = */amountInMax(slippageTolerance),
                /* amountOut = */amountOut,
                /* path = */path,
                /* deadline = */deadline,
                /* gasProvider = */gasProvider,
                /* recipient = */recipient.bigInt
            ).let { hash -> Web3UniLikePendingTransaction(networkClient, hash) }
        }
    }
}
