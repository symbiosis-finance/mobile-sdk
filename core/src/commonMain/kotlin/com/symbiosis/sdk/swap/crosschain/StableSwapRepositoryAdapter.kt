package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.TokenAmount

interface StableSwapRepositoryAdapter {
    suspend fun findBestTrade(amountIn: TokenAmount): StableSwapTradeAdapter
}
