package com.symbiosis.sdk.swap.singleNetwork

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkSwapRepository.ExactInResult
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkSwapRepository.ExactOutResult
import dev.icerock.moko.web3.EthereumAddress

internal class ComposedExactInAdapter(
    private val first: SingleNetworkSwapRepository.ExactInAdapter,
    private val second: SingleNetworkSwapRepository.ExactInAdapter
) : SingleNetworkSwapRepository.ExactInAdapter {
    override suspend fun exactIn(
        amountIn: BigInt,
        tokens: SingleNetworkTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress
    ): ExactInResult {
        val firstTradeResult = first.exactIn(amountIn, tokens, slippageTolerance, from, recipient)
        val secondTradeResult = second.exactIn(amountIn, tokens, slippageTolerance, from, recipient)

        val firstTrade = when (firstTradeResult) {
            is ExactInResult.Success -> firstTradeResult.trade
            else -> return secondTradeResult
        }

        val secondTrade = when (secondTradeResult) {
            is ExactInResult.Success -> secondTradeResult.trade
            else -> return firstTradeResult
        }

        return when (firstTrade.amountOutEstimated > secondTrade.amountOutEstimated) {
            true -> firstTradeResult
            false -> secondTradeResult
        }
    }
}

internal class ComposedExactOutAdapter(
    private val first: SingleNetworkSwapRepository.ExactOutAdapter,
    private val second: SingleNetworkSwapRepository.ExactOutAdapter
) : SingleNetworkSwapRepository.ExactOutAdapter {
    override suspend fun exactOut(
        amountOut: BigInt,
        tokens: SingleNetworkTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress
    ): ExactOutResult {
        val firstTradeResult = first.exactOut(amountOut, tokens, slippageTolerance, from, recipient)
        val secondTradeResult = second.exactOut(amountOut, tokens, slippageTolerance, from, recipient)

        val firstTrade = when {
            firstTradeResult is ExactOutResult.Success -> firstTradeResult.trade
            secondTradeResult is ExactOutResult.Success -> return secondTradeResult
            // insufficient liquidity has more priority than trade not found
            firstTradeResult is ExactOutResult.InsufficientLiquidity -> return firstTradeResult
            else -> return secondTradeResult
        }

        val secondTrade = when (secondTradeResult) {
            is ExactOutResult.Success -> secondTradeResult.trade
            else -> return firstTradeResult
        }

        return when (firstTrade.amountInEstimated < secondTrade.amountInEstimated) {
            true -> firstTradeResult
            false -> secondTradeResult
        }
    }
}
