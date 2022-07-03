package com.symbiosis.sdk.swap.uni

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.internal.kbignum.toBigNum
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.uni.UniLikeSwapRepository.CalculatedRoute

class Web3UniLikeSwapCalculatedRoute(
    private val networkClient: NetworkClient,
    override val dexEndpoint: DexEndpoint,
    override val pools: List<UniLikePool>,
    override val tokens: NetworkTokenPair // this may also include native tokens
) : CalculatedRoute {
    override fun exactIn(amountIn: BigInt): UniLikeTrade.ExactIn {
        val tradeFee = tradeFee(
            amountInWithoutFee = amountIn,
            poolsCount = pools.size,
            liquidityProviderFeePercent = dexEndpoint.liquidityProviderFeePercent
        )

        val amountInWithFee = amountIn - tradeFee

        val amountOutWithoutImpact = pools.amountOutWithoutFee(amountInWithFee, hasImpact = false)
        val amountOut = pools.amountOutWithoutFee(amountInWithFee, hasImpact = true)

        val priceImpact = 1.bn - amountOut.toBigNum().div(amountOutWithoutImpact.toBigNum(), precision = 18)

        return UniLikeTrade.ExactIn(
            networkClient = networkClient,
            fee = TokenAmount(tradeFee, tokens.first),
            priceImpact = Percentage(priceImpact),
            amountIn = TokenAmount(amountIn, tokens.first),
            amountOutEstimated = TokenAmount(amountOut, tokens.second),
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
            trade = UniLikeTrade.ExactOut(
                networkClient = networkClient,
                fee = TokenAmount(tradeFee, tokens.first),
                priceImpact = Percentage(priceImpact),
                amountOut = TokenAmount(amountOut, tokens.second),
                amountInEstimated = TokenAmount(amountIn, tokens.first),
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

    override fun toString(): String = "UniLikeRoute[${pools.joinToString(separator = " -> ")}]"
}


