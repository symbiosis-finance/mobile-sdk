package com.symbiosis.sdk.configuration

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.swap.crosschain.NerveStablePool

/**
 * This one allows you to provide custom stable pools
 */
fun interface StablePoolProvider {
    suspend fun getStablePool(firstChainId: BigInt, secondChainId: BigInt): NerveStablePool
}
