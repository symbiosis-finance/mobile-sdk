package com.symbiosis.sdk.swap.singleNetwork

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.internal.kbignum.toBigNum
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.oneInch.OneInchSwapRepository
import com.symbiosis.sdk.swap.oneInch.OneInchTrade
import com.symbiosis.sdk.swap.oneInch.asNetworkPair
import com.symbiosis.sdk.swap.oneInch.asToken
import com.symbiosis.sdk.swap.uni.UniLikeTrade
import com.symbiosis.sdk.transaction.Web3SwapTransaction
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.signing.Credentials

interface SingleNetworkTrade {
    val slippageTolerance: Percentage
    val priceImpact: Percentage
    val fee: TokenAmount
    val recipient: EthereumAddress
    val routerAddress: ContractAddress
    val value: BigInt
    val tokens: NetworkTokenPair
    val callData: HexString
    val callDataOffset: BigInt
    val path: List<DecimalsToken>

    suspend fun execute(credentials: Credentials): Web3SwapTransaction

    sealed interface ExactIn : SingleNetworkTrade {
        val amountIn: TokenAmount
        val amountOutEstimated: TokenAmount
        val amountOutMin: TokenAmount
    }

    sealed interface ExactOut : SingleNetworkTrade {
        val amountOut: TokenAmount
        val amountInEstimated: TokenAmount
        val amountInMax: TokenAmount
    }

    sealed class UniLike(
        private val underlying: UniLikeTrade,
        private val slippageTolerance: Percentage,
        private val recipient: EthereumAddress
    ) {
        val path = underlying.path
        val callDataOffset = underlying.callDataOffset
        val fee = underlying.fee
        val priceImpact = underlying.priceImpact

        @Suppress("RedundantSuspendModifier") // required for override
        suspend fun recalculateExactIn(amountIn: BigInt): SingleNetworkTrade.ExactIn {
            val trade = underlying.recalculateExactIn(amountIn)
            val callData = trade.callData(slippageTolerance, recipient)

            return ExactIn(trade, slippageTolerance, recipient, callData)
        }

        suspend fun execute(credentials: Credentials) = underlying.execute(
            credentials, slippageTolerance, recipient = recipient
        )

        data class ExactIn(
            val underlying: UniLikeTrade.ExactIn,
            override val slippageTolerance: Percentage,
            override val recipient: EthereumAddress,
            override val callData: HexString
        ) : UniLike(underlying, slippageTolerance, recipient), SingleNetworkTrade.ExactIn {
            override val amountIn = underlying.amountIn
            override val amountOutEstimated = underlying.amountOutEstimated
            override val amountOutMin = underlying.amountOutMin(slippageTolerance)

            override val routerAddress = underlying.routerAddress
            override val value = underlying.value
            override val tokens = underlying.tokens

        }
        data class ExactOut(
            val underlying: UniLikeTrade.ExactOut,
            override val slippageTolerance: Percentage,
            override val callData: HexString,
            override val recipient: EthereumAddress
        ) : UniLike(underlying, slippageTolerance, recipient), SingleNetworkTrade.ExactOut {
            override val amountOut = underlying.amountOut
            override val amountInEstimated = underlying.amountInEstimated
            override val amountInMax = underlying.amountInMax(slippageTolerance)

            override val routerAddress = underlying.routerAddress
            override val value = underlying.value(slippageTolerance)
            override val tokens = underlying.tokens
        }
    }

    data class OneInch(
        val underlying: OneInchTrade,
        private val oneInchSwapRepository: OneInchSwapRepository,
        private val network: Network
    ) : ExactIn {
        override val path: List<DecimalsToken> = listOf(
            underlying.tokens.first.asToken(network),
            underlying.tokens.second.asToken(network)
        )
        override val slippageTolerance = underlying.slippageTolerance

        override val amountIn = underlying.amountIn
        override val amountOutEstimated = underlying.amountOutEstimated
        override val amountOutMin: TokenAmount

        init {
            val amountOutEstimatedInt = amountOutEstimated.raw
            val amountOutMinInt = (amountOutEstimatedInt.toBigNum() * (1.bn - slippageTolerance.fractionalValue))
                .toBigInt()
            amountOutMin = TokenAmount(amountOutMinInt, amountOutEstimated.token)
        }

        // https://github.com/symbiosis-finance/js-sdk/blob/8fa705c582aef82c97ff5b37b802dfd1ba829b18/src/crosschain/oneInchTrade.ts#L124
        override val priceImpact: Percentage = underlying.priceImpact
        override val fee = TokenAmount(0.bn, network.nativeCurrency)

        override val recipient = underlying.recipient
        override val routerAddress = underlying.to
        override val callData = underlying.callData
        override val value = underlying.value
        override val tokens = underlying.tokens.asNetworkPair(network)

        override val callDataOffset: BigInt = underlying.callDataOffset

        override suspend fun execute(credentials: Credentials): Web3SwapTransaction =
            underlying.execute(credentials)
    }
}
