package com.symbiosis.sdk.configuration

import com.soywiz.kbignum.BigInt

/**
 * This one allows you to provide a custom default swap time-to-live
 */
fun interface SwapTTLProvider {
    suspend fun getSwapTTL(chainId: BigInt): BigInt
}
