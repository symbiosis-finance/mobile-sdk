package com.symbiosis.sdk.swap.uni

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.network.NetworkClient

class Web3UniLikeSwapCalculatedRoutes(
    private val networkClient: NetworkClient,
    private val routes: List<UniLikeSwapRepository.CalculatedRoute>
) : UniLikeSwapRepository.CalculatedRoutes {

    override fun bestTradeExactIn(amountIn: BigInt): UniLikeSwapRepository.ExactInResult {
        return routes.fold(
            initial = UniLikeSwapRepository.ExactInResult.TradeNotFound
        ) { tradeResult: UniLikeSwapRepository.ExactInResult, tradeRoute ->
            val trade = tradeRoute.exactIn(amountIn)

            if (tradeResult !is UniLikeSwapRepository.ExactInResult.Success)
                return@fold UniLikeSwapRepository.ExactInResult.Success(trade)

            return@fold when (tradeResult.trade.amountOutEstimated.raw > trade.amountOutEstimated.raw) {
                true -> tradeResult
                false -> UniLikeSwapRepository.ExactInResult.Success(trade)
            }
        }
    }

    override fun bestTradeExactOut(amountOut: BigInt): UniLikeSwapRepository.ExactOutResult {
        return routes.fold(
            initial = UniLikeSwapRepository.ExactOutResult.TradeNotFound
        ) { currentTradeResult: UniLikeSwapRepository.ExactOutResult, tradeRoute ->
            val tradeResult = when(val tradeResult = tradeRoute.exactOut(amountOut)) {
                is UniLikeSwapRepository.CalculatedRoute.ExactOutResult.Success ->
                    UniLikeSwapRepository.ExactOutResult.Success(tradeResult.trade)
                UniLikeSwapRepository.CalculatedRoute.ExactOutResult.InsufficientLiquidity ->
                    UniLikeSwapRepository.ExactOutResult.InsufficientLiquidity
            }

            if (currentTradeResult !is UniLikeSwapRepository.ExactOutResult.Success)
                return@fold tradeResult

            if (tradeResult !is UniLikeSwapRepository.ExactOutResult.Success)
                return@fold currentTradeResult

            return@fold when (currentTradeResult.trade.amountInEstimated.raw < tradeResult.trade.amountInEstimated.raw) {
                true -> currentTradeResult
                false -> tradeResult
            }
        }
    }
}
