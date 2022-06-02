package com.symbiosis.sdk.swap.singleNetwork

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.oneInch.OneInchSwapRepository
import com.symbiosis.sdk.swap.oneInch.OneInchTrade
import com.symbiosis.sdk.swap.oneInch.asNetworkPair
import com.symbiosis.sdk.swap.uni.UniLikeSwapRepository
import com.symbiosis.sdk.swap.uni.UniLikeTrade
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.hex.HexString

sealed interface SingleNetworkTrade {
    val slippageTolerance: Percentage
    val priceImpact: Percentage
    val fee: BigInt
    val recipient: EthereumAddress
    val routerAddress: ContractAddress
    val value: BigInt
    val tokens: NetworkTokenPair
    val callData: HexString
    val callDataOffset: BigInt

    suspend fun recalculateExactIn(amountIn: BigInt): ExactIn

    sealed interface ExactIn : SingleNetworkTrade {
        val amountIn: BigInt
        val amountOutEstimated: BigInt
        val amountOutMin: BigInt
    }

    sealed interface ExactOut : SingleNetworkTrade {
        val amountOut: BigInt
        val amountInEstimated: BigInt
        val amountInMax: BigInt
    }

    sealed class UniLike(
        private val underlying: UniLikeTrade,
        private val slippageTolerance: Percentage,
        private val recipient: EthereumAddress
    ) {
        val callDataOffset = underlying.callDataOffset
        val fee = underlying.fee
        val priceImpact = underlying.priceImpact

        @Suppress("RedundantSuspendModifier") // required for override
        suspend fun recalculateExactIn(amountIn: BigInt): SingleNetworkTrade.ExactIn {
            val trade = underlying.recalculateExactIn(amountIn)
            val callData = trade.callData(slippageTolerance, recipient)

            return ExactIn(trade, slippageTolerance, recipient, callData)
        }

        suspend fun recalculateExactOut(amountOut: BigInt): SingleNetworkSwapRepository.ExactOutResult {
            val trade = when (val result = underlying.recalculateExactOut(amountOut)) {
                is UniLikeSwapRepository.CalculatedRoute.ExactOutResult.Success ->
                    result.trade
                is UniLikeSwapRepository.CalculatedRoute.ExactOutResult.InsufficientLiquidity ->
                    return SingleNetworkSwapRepository.ExactOutResult.InsufficientLiquidity
            }
            val callData = trade.callData(slippageTolerance, recipient)

            return SingleNetworkSwapRepository.ExactOutResult.Success(
                trade = ExactOut(trade, slippageTolerance, callData, recipient)
            )
        }

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
        override val slippageTolerance = underlying.slippageTolerance

        override val amountIn = underlying.amountIn
        override val amountOutEstimated = underlying.amountOutEstimated
        override val amountOutMin =
            (underlying.amountOutEstimated.toBigNum() * (1.bn - slippageTolerance.fractionalValue))
                .toBigInt()

        // https://github.com/symbiosis-finance/js-sdk/blob/8fa705c582aef82c97ff5b37b802dfd1ba829b18/src/crosschain/oneInchTrade.ts#L124
        override val priceImpact: Percentage = underlying.priceImpact
        override val fee = 0.bi

        override val recipient = underlying.recipient
        override val routerAddress = underlying.to
        override val callData = underlying.callData
        override val value = underlying.value
        override val tokens = underlying.tokens.asNetworkPair(network)

        override suspend fun recalculateExactIn(amountIn: BigInt): ExactIn =
            with (underlying) {
                val trade = oneInchSwapRepository
                    .exactIn(amountIn, tokens, slippageTolerance, fromAddress, recipient)
                OneInch(underlying, oneInchSwapRepository, network)
            }

        override val callDataOffset: BigInt = underlying.callDataOffset
    }
}
