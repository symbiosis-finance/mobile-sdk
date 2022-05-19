package com.symbiosis.sdk.swap.uni

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.swap.ExecutableTrade
import com.symbiosis.sdk.swap.ReservesData
import com.symbiosis.sdk.swap.trade.Web3UniLikeTrade
import com.symbiosis.sdk.swap.uni.UniLikeSwapCalculator.CalculatedRoute

class Web3UniLikeCalculatedRoute(
    private val networkClient: NetworkClient,
    val dexEndpoint: DexEndpoint,
    val pools: List<Web3UniLikePool>,
    val pair: NetworkTokenPair // this may also include native tokens
) : CalculatedRoute {
    override fun exactIn(amountIn: BigInt): ExecutableTrade.ExactIn {
        val tradeFee = tradeFee(
            amountInWithoutFee = amountIn,
            poolsCount = pools.size,
            liquidityProviderFeePercent = dexEndpoint.liquidityProviderFeePercent
        )

        val amountInWithFee = amountIn - tradeFee

        val amountOutWithoutImpact = pools.amountOutWithoutFee(amountInWithFee, hasImpact = false)
        val amountOut = pools.amountOutWithoutFee(amountInWithFee, hasImpact = true)

        val priceImpact = 1.bn - amountOut.toBigNum().div(amountOutWithoutImpact.toBigNum(), precision = 18)

        return Web3UniLikeTrade.ExactIn(
            networkClient = networkClient,
            fee = tradeFee,
            priceImpact = priceImpact,
            amountIn = amountIn,
            amountOutEstimated = amountOut,
            route = this
        )
    }

    override fun exactOut(amountOut: BigInt): CalculatedRoute.ExactOutResult {

        // without impact there is no way to get insufficient liquidity error
        val amountInWithoutImpact = (pools.amountInWithoutFee(amountOut, hasImpact = false)
                as AmountInWithoutFeeResult.Success).amountIn
        val amountInWithoutFeeResult = pools.amountInWithoutFee(amountOut, hasImpact = true)

        if (amountInWithoutFeeResult !is AmountInWithoutFeeResult.Success)
            return CalculatedRoute.ExactOutResult.InsufficientLiquidity

        val amountInWithoutFee = amountInWithoutFeeResult.amountIn

        val tradeFee = tradeFee(
            amountInWithoutFee = amountInWithoutFee,
            poolsCount = pools.size,
            liquidityProviderFeePercent = dexEndpoint.liquidityProviderFeePercent
        )

        val amountIn = amountInWithoutFee + tradeFee

        val priceImpact = 1.bn - amountInWithoutImpact.toBigNum().div(amountInWithoutFee.toBigNum(), precision = 18)

        return CalculatedRoute.ExactOutResult.Success(
            trade = Web3UniLikeTrade.ExactOut(
                networkClient = networkClient,
                fee = tradeFee,
                priceImpact = priceImpact,
                amountOut = amountOut,
                amountInEstimated = amountIn,
                route = this
            )
        )
    }

    /**
     * Every pool on route reduces [liquidityProviderFeePercent], so this logic
     * is implemented here
     */
    fun tradeFee(
        amountInWithoutFee: BigInt,
        poolsCount: Int,
        liquidityProviderFeePercent: BigNum
    ): BigInt {
        val multiplier = 1.bn - liquidityProviderFeePercent
        val taxPercent = 1.bn - (1..poolsCount).fold(initial = 1.bn) { acc, _ -> acc * multiplier }
        return (amountInWithoutFee.toBigNum() * taxPercent).toBigInt()
    }
}

data class Web3UniLikePool(val pair: NetworkTokenPair.Erc20Only, val reserves: ReservesData) {
    fun outputAmountWithoutFee(amountIn: BigInt, hasImpact: Boolean): BigInt {
        val newReserves = when (hasImpact) {
            true -> reserves.copy(reserve1 = reserves.reserve1 + amountIn)
            false -> reserves
        }
        return amountIn.toBigNum().div(newReserves.price2, precision = 18).toBigInt()
    }

    sealed interface InputAmountResult {
        object InsufficientLiquidity : InputAmountResult
        class Success(val amountIn: BigInt) : InputAmountResult
    }

    // without impact there is no way to get insufficient liquidity error
    fun inputAmountWithoutFee(amountOut: BigInt, hasImpact: Boolean): InputAmountResult {
        val newReserves = when (hasImpact) {
            true -> {
                val newReserve2 = reserves.reserve2 - amountOut
                if (newReserve2 <= 0.bi) return InputAmountResult.InsufficientLiquidity
                reserves.copy(reserve2 = newReserve2)
            }
            false -> reserves
        }
        val amountIn = amountOut.toBigNum().div(newReserves.price1, precision = 18).toBigInt()
        return InputAmountResult.Success(amountIn)
    }
}

fun List<Web3UniLikePool>.amountOutWithoutFee(amountIn: BigInt, hasImpact: Boolean) =
    fold(initial = amountIn) { currentAmountIn, pool ->
        pool.outputAmountWithoutFee(currentAmountIn, hasImpact)
    }

sealed interface AmountInWithoutFeeResult {
    object InsufficientLiquidity : AmountInWithoutFeeResult
    class Success(val amountIn: BigInt) : AmountInWithoutFeeResult
}

// without impact there is no way to get insufficient liquidity error
fun List<Web3UniLikePool>.amountInWithoutFee(amountOut: BigInt, hasImpact: Boolean): AmountInWithoutFeeResult {
    return asReversed().fold(initial = AmountInWithoutFeeResult.Success(amountOut)) { currentAmountInResult, pool ->
        when (val amountInResult = pool.inputAmountWithoutFee(currentAmountInResult.amountIn, hasImpact)) {
            is Web3UniLikePool.InputAmountResult.InsufficientLiquidity ->
                return AmountInWithoutFeeResult.InsufficientLiquidity
            is Web3UniLikePool.InputAmountResult.Success ->
                AmountInWithoutFeeResult.Success(amountInResult.amountIn)
        }
    }
}
