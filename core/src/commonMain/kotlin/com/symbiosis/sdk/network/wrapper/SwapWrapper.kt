package com.symbiosis.sdk.network.wrapper

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.swap.ExactInSingleNetworkTradeCalculator
import com.symbiosis.sdk.swap.ExactInSingleNetworkTradeCalculator.ExactInResult
import com.symbiosis.sdk.swap.ExactOutSingleNetworkTradeCalculator
import com.symbiosis.sdk.swap.ExactOutSingleNetworkTradeCalculator.ExactOutResult

/**
 * High-level wrapper around Swap Contracts
 */
class SwapWrapper internal constructor(
    private val exactInTradeCalculators: List<ExactInSingleNetworkTradeCalculator>,
    private val exactOutTradeCalculators: List<ExactOutSingleNetworkTradeCalculator>
){
    suspend fun findBestTradeExactIn(
        amountIn: BigInt,
        tokens: NetworkTokenPair
    ): ExactInResult<*> =
        exactInTradeCalculators
            .map { calculator -> calculator.exactIn(amountIn, tokens) }
            .findBestTrade()

    suspend fun findBestTradeExactOut(
        amountOut: BigInt,
        tokens: NetworkTokenPair
    ): ExactOutResult<*> =
        exactOutTradeCalculators
            .map { calculator -> calculator.exactOut(amountOut, tokens) }
            .findBestTrade()

}

fun List<ExactInResult<*>>.findBestTrade(): ExactInResult<*> =
    fold(initial = ExactInResult.TradeNotFound) { bestTradeResult: ExactInResult<*>, tradeResult: ExactInResult<*> ->
        if (bestTradeResult !is ExactInResult.Success)
            return@fold tradeResult

        if (tradeResult !is ExactInResult.Success)
            return@fold bestTradeResult

        return@fold when (bestTradeResult.trade.amountOutEstimated < tradeResult.trade.amountOutEstimated) {
            true -> tradeResult
            false -> bestTradeResult
        }
    }

fun List<ExactOutResult<*>>.findBestTrade(): ExactOutResult<*> =
    fold(initial = ExactOutResult.TradeNotFound) { bestTradeResult: ExactOutResult<*>, tradeResult: ExactOutResult<*> ->
        if (bestTradeResult !is ExactOutResult.Success)
            return@fold tradeResult

        if (tradeResult !is ExactOutResult.Success)
            return@fold bestTradeResult

        return@fold when (bestTradeResult.trade.amountInEstimated > tradeResult.trade.amountInEstimated) {
            true -> tradeResult
            false -> bestTradeResult
        }
    }
