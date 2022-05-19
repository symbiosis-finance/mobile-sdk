package com.symbiosis.sdk.swap.uni

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.ClientsManager
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.swap.ExactInSingleNetworkTradeCalculator.ExactInResult
import com.symbiosis.sdk.swap.ExactOutSingleNetworkTradeCalculator.ExactOutResult
import com.symbiosis.sdk.swap.SingleNetworkTradeCalculator
import com.symbiosis.sdk.swap.trade.Web3UniLikeTrade

class UniLikeSwapCalculator(
    private val router: UniLikeRouter? = null
) : SingleNetworkTradeCalculator {
    override suspend fun exactIn(
        amountIn: BigInt, tokens: NetworkTokenPair
    ): ExactInResult<Web3UniLikeTrade.ExactIn> {
        val bestTrade: Web3UniLikeTrade.ExactIn =
            calculateRoutes(tokens)
                .findBestTradeExactIn(amountIn)
                ?: return ExactInResult.TradeNotFound

        return ExactInResult.Success(bestTrade)
    }

    private fun List<CalculatedRoute>.findBestTradeExactIn(amountIn: BigInt): Web3UniLikeTrade.ExactIn? =
        fold(initial = null) { acc: Web3UniLikeTrade.ExactIn?, route ->
            val trade = route.exactIn(amountIn)
            acc ?: return@fold trade

            return@fold when (acc.amountOutEstimated < trade.amountOutEstimated) {
                true -> trade
                false -> acc
            }
        }

    override suspend fun exactOut(
        amountOut: BigInt, tokens: NetworkTokenPair
    ): ExactOutResult<Web3UniLikeTrade.ExactOut> {
        val bestTrade: CalculatedRoute.ExactOutResult =
            calculateRoutes(tokens)
                .findBestTradeExactOut(amountOut)
                ?: return ExactOutResult.TradeNotFound

        return when (bestTrade) {
            is CalculatedRoute.ExactOutResult.Success ->
                ExactOutResult.Success(bestTrade.trade)
            CalculatedRoute.ExactOutResult.InsufficientLiquidity ->
                ExactOutResult.InsufficientLiquidity
        }
    }

    private fun List<CalculatedRoute>.findBestTradeExactOut(amountOut: BigInt): CalculatedRoute.ExactOutResult? =
        fold(initial = null) { acc: CalculatedRoute.ExactOutResult?, route ->
            val tradeResult = route.exactOut(amountOut)

            if (tradeResult !is CalculatedRoute.ExactOutResult.Success)
                return@fold acc

            return@fold when (acc) {
                null, is CalculatedRoute.ExactOutResult.InsufficientLiquidity -> tradeResult
                is CalculatedRoute.ExactOutResult.Success ->
                    when (acc.trade.amountInEstimated > tradeResult.trade.amountInEstimated) {
                        true -> tradeResult
                        false -> acc
                    }
            }
        }

    private suspend fun calculateRoutes(tokens: NetworkTokenPair): List<CalculatedRoute> =
        (router ?: Web3UniLikeRouter(ClientsManager.getNetworkClient(tokens.network)))
            .buildRoutes(tokens)
            .fetch()

    interface UniLikeRouter {
        fun buildRoutes(pair: NetworkTokenPair): Routes
    }
    interface Routes {
        suspend fun fetch(): List<CalculatedRoute>
    }
    interface CalculatedRoute {
        fun exactIn(amountIn: BigInt): Web3UniLikeTrade.ExactIn
        fun exactOut(amountOut: BigInt): ExactOutResult

        sealed interface ExactOutResult {
            class Success(val trade: Web3UniLikeTrade.ExactOut) : ExactOutResult
            object InsufficientLiquidity : ExactOutResult
        }
    }
}
