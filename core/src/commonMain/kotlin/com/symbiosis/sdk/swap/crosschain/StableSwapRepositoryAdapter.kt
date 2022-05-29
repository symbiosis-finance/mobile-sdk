package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt

interface StableSwapRepositoryAdapter {
    suspend fun findBestTrade(amountIn: BigInt): StableSwapTradeAdapter
}
