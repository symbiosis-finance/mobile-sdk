package com.symbiosis.sdk.swap

import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.dex.DexEndpoint
import kotlin.jvm.JvmInline

/**
 * A class that represents swap route with known reserves
 */
@JvmInline
value class SwapRouteState(val value: List<SwapPoolState>) {
    init {
        require(value.isNotEmpty())
        require(value.all { it.dex == value[0].dex })
    }
    val route get() = SwapRoute(value = listOf(value[0].pair.first) + value.map { it.pair.second })
    val dex get() = value[0].dex
}

internal fun List<Triple<NetworkTokenPair, DexEndpoint, ReservesData>>.asRouteState() = SwapRouteState(
    map { (pair, dex, reserves) -> SwapPoolState(pair, dex, reserves) }
)
