package com.symbiosis.sdk.swap

import com.soywiz.kbignum.BigInt

sealed class SwapTrade(
    val value: BigInt,
    val route: SwapRoute
) {
    class ExactIn(val amountIn: BigInt, route: SwapRoute) : SwapTrade(amountIn, route)
    class ExactOut(val amountOut: BigInt, route: SwapRoute) : SwapTrade(amountOut, route)
}
