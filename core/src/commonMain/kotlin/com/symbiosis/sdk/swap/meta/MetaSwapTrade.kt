package com.symbiosis.sdk.swap.meta

import com.soywiz.kbignum.BigInt

sealed class MetaSwapTrade(
    val value: BigInt,
    val route: MetaSwapRoute
) {
    class ExactIn(val amountIn: BigInt, route: MetaSwapRoute) : MetaSwapTrade(amountIn, route)
    class ExactOut(val amountOut: BigInt, route: MetaSwapRoute) : MetaSwapTrade(amountOut, route)
}
