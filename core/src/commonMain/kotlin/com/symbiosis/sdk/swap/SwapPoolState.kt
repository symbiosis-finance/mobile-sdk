package com.symbiosis.sdk.swap

import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.dex.DexEndpoint

data class SwapPoolState(
    val pair: NetworkTokenPair,
    val dex: DexEndpoint,
    val reserves: ReservesData
)
