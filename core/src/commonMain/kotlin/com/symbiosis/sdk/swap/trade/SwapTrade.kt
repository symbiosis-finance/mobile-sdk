package com.symbiosis.sdk.swap.trade

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum

sealed interface SwapTrade {
    val fee: BigInt
    val priceImpact: BigNum /* 0.0 .. 1.0 */
    val priceImpactPercent: Int get() = priceImpact.convertToScale(otherScale = 2).int.toInt()

    interface ExactIn : SwapTrade {
        val amountIn: BigInt
        val amountOutEstimated: BigInt
    }

    interface ExactOut : SwapTrade {
        val amountOut: BigInt
        val amountInEstimated: BigInt
    }
}
