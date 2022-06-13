package com.symbiosis.sdk.swap.crosschain.nerve

import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.swap.crosschain.CrossChain

fun NerveSwapRepository(crossChain: CrossChain): NerveSwapRepository {
    val adapter = DefaultNerveStablePoolAdapter(crossChain)

    return NerveSwapRepository(adapter)
}

class NerveSwapRepository(private val pool: StablePool) {
    suspend fun findTrade(amountIn: TokenAmount): NerveSwapTrade =
        pool.findTrade(amountIn)

    interface StablePool {
        suspend fun findTrade(amountIn: TokenAmount): NerveSwapTrade
    }
}
