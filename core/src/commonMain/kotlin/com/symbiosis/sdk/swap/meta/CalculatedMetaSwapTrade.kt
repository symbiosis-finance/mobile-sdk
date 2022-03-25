package com.symbiosis.sdk.swap.meta

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.convertIntegerToReal

/**
 * @param stablePoolAmountOutMin stable pool amount out min
 */
sealed interface CalculatedMetaSwapTrade {

    sealed class Success(
        val value: BigInt,
        val route: MetaSwapRoute,
        val stablePoolAmountIn: BigInt,
        val stablePoolAmountOutMin: BigInt,
        val pathFromStableAmountIn: BigInt,
        val targetValue: BigInt,
        val targetValueMin: BigInt,
        /**
         * Slippage that was used to calculate minimal target value
         */
        val slippage: BigNum,
        val bridgingFee: BigInt
    ) : CalculatedMetaSwapTrade {
        init {
            require(slippage < 1.bn && slippage >= 0.bn) { "Tolerance should be in [0;1) range but was $slippage" }
        }

        val price: BigNum get() {
            return value.toBigNum().div(
                // preventing division by zero
                other = targetValue.toBigNum().takeIf { it != 0.bn } ?: return 0.bn,
                precision = 18
            )
        }

        val priceImpact: BigNum
            get() {
                val fromPriceImpact = route.pathToStable?.priceImpact ?: 0.bn
                // get stable price impact from
                // (targetValue - sourceValue) / targetValue
                // and compare it with borders [0, 100] percents

                val stablePoolAmountInNumber =
                    route.firstNetworkStableToken.convertIntegerToReal(stablePoolAmountIn)
                val stablePoolAmountOutMinNumber =
                    route.lastNetworkStableToken.convertIntegerToReal(stablePoolAmountOutMin)

                val difference = (stablePoolAmountInNumber - stablePoolAmountOutMinNumber).takeIf { it != 0.bn }

                val stablePoolPriceImpact: BigNum = difference?.let {
                    val priceImpact = difference.div(stablePoolAmountInNumber, precision = 4)
                    when {
                        priceImpact > 1.bn -> 1.bn
                        priceImpact < 0.bn -> 0.bn
                        else -> priceImpact
                    }
                } ?: 0.bn

                val toPriceImpact = route.pathFromStable?.priceImpact ?: 0.bn
                val finalPriceImpact = fromPriceImpact + stablePoolPriceImpact + toPriceImpact

                // return 100% or current value price impact
                return when {
                    finalPriceImpact < 1.bn -> finalPriceImpact
                    else -> 1.bn
                }
            }

        override fun toString(): String {
            return "CalculatedMetaSwapTrade.Success(value=$value, route=$route, stablePoolAmountIn=$stablePoolAmountIn, stablePoolAmountOutMin=$stablePoolAmountOutMin, pathFromStableAmountIn=$pathFromStableAmountIn, targetValue=$targetValue, targetValueMin=$targetValueMin, slippage=$slippage, bridgingFee=$bridgingFee, price=$price, priceImpact=$priceImpact)"
        }
    }

    class ExactIn(
        private val amountIn: BigInt,
        route: MetaSwapRoute,
        stablePoolAmountIn: BigInt,
        stablePoolAmountOutMin: BigInt,
        pathFromStableAmountIn: BigInt,
        amountOut: BigInt,
        amountOutMin: BigInt,
        slippage: BigNum,
        bridgingFee: BigInt
    ) : Success(
        value = amountIn,
        route = route,
        stablePoolAmountIn = stablePoolAmountIn,
        stablePoolAmountOutMin = stablePoolAmountOutMin,
        pathFromStableAmountIn = pathFromStableAmountIn,
        targetValue = amountOut,
        targetValueMin = amountOutMin,
        slippage = slippage,
        bridgingFee = bridgingFee
    )

    data class StableTokensLessThanMin(
        val tradeStableTokens: BigNum,
        val minStableTokens: BigNum
    ) : CalculatedMetaSwapTrade

    data class StableTokensGreaterThanMax(
        val tradeStableTokens: BigNum,
        val maxStableTokens: BigNum
    ) : CalculatedMetaSwapTrade
}
