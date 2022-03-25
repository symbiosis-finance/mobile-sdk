package com.symbiosis.sdk.swap

import com.soywiz.kbignum.BigInt

/**
 * A class that represents trade with known route reserves
 */
sealed class SwapTradeState(
    val value: BigInt,
    val route: SwapRouteState
) {
    class ExactIn(val amountIn: BigInt, route: SwapRouteState) : SwapTradeState(amountIn, route)
    class ExactOut(val amountOut: BigInt, route: SwapRouteState) : SwapTradeState(amountOut, route)
}
