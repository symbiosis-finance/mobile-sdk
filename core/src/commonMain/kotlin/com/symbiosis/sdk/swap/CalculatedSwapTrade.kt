package com.symbiosis.sdk.swap

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.symbiosis.sdk.dex.DexEndpoint

sealed interface CalculatedSwapTrade : Comparable<CalculatedSwapTrade> {
    sealed class Success(
        val sourceValue: BigInt,
        val targetValue: BigInt,
        val liquidityProviderFee: BigInt,
        val priceImpact: BigNum,
        val route: SwapRoute,
        val dex: DexEndpoint
    ) : CalculatedSwapTrade {
        val price: BigNum
            get() = when (this) {
                is ExactIn -> sourceValue.toBigNum().div(targetValue.toBigNum(), precision = 18)
                is ExactOut -> targetValue.toBigNum().div(sourceValue.toBigNum(), precision = 18)
            }

        override fun toString(): String =
            "CalculatedSwapTrade.Success(sourceValue=$sourceValue,targetValue=$targetValue,liquidityProviderFee=$liquidityProviderFee,price=$price,priceImpact=$priceImpact,route=$route)"
    }

    class ExactIn(
        val amountIn: BigInt,
        val amountOut: BigInt,
        liquidityProviderFee: BigInt,
        priceImpact: BigNum,
        route: SwapRoute,
        dex: DexEndpoint
    ) : Success(
        sourceValue = amountIn,
        targetValue = amountOut,
        liquidityProviderFee = liquidityProviderFee,
        priceImpact = priceImpact,
        route = route,
        dex = dex
    )

    sealed interface ExactOut : CalculatedSwapTrade {
        object InsufficientLiquidity : ExactOut

        class Success(
            val amountIn: BigInt,
            val amountOut: BigInt,
            liquidityProviderFee: BigInt,
            priceImpact: BigNum,
            route: SwapRoute,
            dex: DexEndpoint
        ) : CalculatedSwapTrade.Success(
            sourceValue = amountOut,
            targetValue = amountIn,
            liquidityProviderFee = liquidityProviderFee,
            priceImpact = priceImpact,
            route = route,
            dex = dex
        ), ExactOut
    }

    override fun compareTo(other: CalculatedSwapTrade): Int = when {
        this is ExactOut.InsufficientLiquidity && other is ExactOut.InsufficientLiquidity -> 0
        this is ExactOut.InsufficientLiquidity -> -1
        other is ExactOut.InsufficientLiquidity -> 1
        this is Success && other is Success -> targetValue.compareTo(other.targetValue)
        else -> error("Invalid state")
    }
}


fun CalculatedSwapTrade.Success.targetValueWithTolerance(
    slippageTolerance: BigNum
) = (targetValue.toBigNum() * slippageTolerance).toBigInt().let { slippage ->
    when (this) {
        is CalculatedSwapTrade.ExactIn -> targetValue - slippage
        is CalculatedSwapTrade.ExactOut -> targetValue + slippage
    }
}

fun CalculatedSwapTrade.ExactIn.amountOutMin(
    slippageTolerance: BigNum
) = targetValueWithTolerance(slippageTolerance)

fun CalculatedSwapTrade.ExactOut.Success.amountInMax(
    slippageTolerance: BigNum
) = targetValueWithTolerance(slippageTolerance)
