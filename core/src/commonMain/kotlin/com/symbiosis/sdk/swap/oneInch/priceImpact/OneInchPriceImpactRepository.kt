package com.symbiosis.sdk.swap.oneInch.priceImpact

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.oneInch.OneInchTokenPair

class OneInchPriceImpactRepository(private val adapter: Adapter) {
    suspend fun priceImpact(tokens: OneInchTokenPair, amountIn: BigInt, amountOut: BigInt): Percentage {
        // (1): token1 / eth = rate1 | div (1) by (2)
        // (2): token2 / eth = rate2 |       =>
        //
        // => token1 / token2 = rate1 / rate2 (3)
        //
        // token1Amount * token2 / token1 = token2AmountNoImpact

        val (inputTokenToEth, outputTokenToEth) = adapter
            .getRateRequests(tokens)
            .fetch()

        val amountOutNoImpact = TokenAmount(amountIn, tokens.first.decimals)
            .amount
            // conversion required, so we will have more precise calculations
            .convertToScale(otherScale = 18) * inputTokenToEth / outputTokenToEth

        val amountOutBigNum = TokenAmount(amountOut, tokens.second.decimals).amount

        return Percentage(fractionalValue = 1.bn - amountOutBigNum.div(amountOutNoImpact, precision = 4))
    }

    interface Adapter {
        fun getRateRequests(tokens: OneInchTokenPair): RateRequests
    }

    interface RateRequests {
        suspend fun fetch(): TokenAmounts
    }

    data class TokenAmounts(
        val inputTokenRateToEth: BigNum,
        val outputTokenRateToEth: BigNum
    )
}
