package com.symbiosis.sdk.swap.uni

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.dex.DexEndpoint

class UniLikeSwapRepository(private val router: Router) {
    sealed interface ExactInResult {
        object TradeNotFound : ExactInResult
        class Success(val trade: UniLikeTrade.ExactIn) : ExactInResult
    }

    suspend fun exactIn(amountIn: BigInt, tokens: NetworkTokenPair) =
        calculateRoutes(tokens)
            .bestTradeExactIn(amountIn)

    sealed interface ExactOutResult {
        object TradeNotFound : ExactOutResult
        object InsufficientLiquidity : ExactOutResult
        class Success(val trade: UniLikeTrade.ExactOut) : ExactOutResult
    }

    suspend fun exactOut(amountOut: BigInt, tokens: NetworkTokenPair) =
        calculateRoutes(tokens)
            .bestTradeExactOut(amountOut)

    private suspend fun calculateRoutes(tokens: NetworkTokenPair): CalculatedRoutes =
        router
            .buildRoutes(tokens)
            .fetch()

    interface Router {
        fun buildRoutes(tokens: NetworkTokenPair): Routes
    }

    interface Routes {
        suspend fun fetch(): CalculatedRoutes
    }

    interface CalculatedRoutes {
        fun bestTradeExactIn(amountIn: BigInt): ExactInResult
        fun bestTradeExactOut(amountOut: BigInt): ExactOutResult
    }

    interface CalculatedRoute {
        val pools: List<UniLikePool>
        val dexEndpoint: DexEndpoint
        val tokens: NetworkTokenPair

        fun exactIn(amountIn: BigInt): UniLikeTrade.ExactIn
        fun exactOut(amountOut: BigInt): ExactOutResult

        sealed interface ExactOutResult {
            class Success(val trade: UniLikeTrade.ExactOut) : ExactOutResult
            object InsufficientLiquidity : ExactOutResult
        }
    }
}
