package com.symbiosis.sdk.swap.crosschain

import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.swap.Percentage

interface StableSwapRepositoryAdapter {
    suspend fun findBestTrade(amountIn: TokenAmount, slippageTolerance: Percentage): StableSwapTradeAdapter
}
