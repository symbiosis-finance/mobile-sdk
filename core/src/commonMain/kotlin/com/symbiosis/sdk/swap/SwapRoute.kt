package com.symbiosis.sdk.swap

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.Token
import kotlin.jvm.JvmInline

@JvmInline
value class SwapRoute(val value: List<Token>) {
    init {
        require(value.isNotEmpty()) { "Swap route should not be empty" }
        require(value.all { it.network.chainId == value[0].network.chainId }) {
            "All currencies should be from the one network"
        }
    }
    val network get() = value[0].network
    val pairs get() = value.windowed(size = 2).map { (first, second) -> NetworkTokenPair(first, second) }
}

fun List<SwapRoute>.mapToExactInTrade(amountIn: BigInt) = map { SwapTrade.ExactIn(amountIn, it) }
fun List<SwapRoute>.mapToExactOutTrade(amountOut: BigInt) = map { SwapTrade.ExactOut(amountOut, it) }
