package com.symbiosis.sdk.configuration

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.dex.DexEndpoint

/**
 * This one allows you to provide custom dex endpoints
 */
fun interface DexProvider {
    suspend fun getDexEndpoints(chainId: BigInt): List<DexEndpoint>
}