package com.symbiosis.sdk.swap

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.swap.trade.SwapTrade

interface SingleNetworkTradeCalculator : ExactInSingleNetworkTradeCalculator, ExactOutSingleNetworkTradeCalculator

interface ExactInSingleNetworkTradeCalculator {
    sealed interface ExactInResult<out Trade : SwapTrade.ExactIn> {
        object TradeNotFound : ExactInResult<Nothing>
        class Success<out Trade : SwapTrade.ExactIn>(val trade: Trade) : ExactInResult<Trade>
    }
    suspend fun exactIn(amountIn: BigInt, tokens: NetworkTokenPair): ExactInResult<*>
}

interface ExactOutSingleNetworkTradeCalculator {
    sealed interface ExactOutResult<out Trade : SwapTrade.ExactOut> {
        object TradeNotFound : ExactOutResult<Nothing>
        object InsufficientLiquidity : ExactOutResult<Nothing>
        class Success<out Trade : SwapTrade.ExactOut>(val trade: Trade) : ExactOutResult<Trade>
    }
    suspend fun exactOut(amountOut: BigInt, tokens: NetworkTokenPair): ExactOutResult<*>
}

