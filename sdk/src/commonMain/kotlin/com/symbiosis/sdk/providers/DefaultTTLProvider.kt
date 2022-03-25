package com.symbiosis.sdk.providers

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.SwapTTLProvider
import com.symbiosis.sdk.internal.time.minutesAsMillis

object DefaultTTLProvider : SwapTTLProvider {
    val TTL: BigInt = 20.minutesAsMillis.bi
    override suspend fun getSwapTTL(chainId: BigInt): BigInt = TTL
}