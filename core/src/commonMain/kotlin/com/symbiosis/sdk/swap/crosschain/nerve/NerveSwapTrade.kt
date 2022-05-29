package com.symbiosis.sdk.swap.crosschain.nerve

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.swap.crosschain.CrossChain
import com.symbiosis.sdk.currency.amount
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.wallet.Credentials

data class NerveSwapTrade(
    val amountIn: BigInt,
    val amountOutEstimated: BigInt,
    val crossChain: CrossChain,
    val networkClient: NetworkClient,
    val tokenIndexFrom: BigInt,
    val tokenIndexTo: BigInt,
    val tokens: NerveTokenPair
) {
    val priceImpact: Percentage

    init {
        val amountInAsReal = tokens.first.amount(raw = amountIn).amount
        val amountOutAsReal = tokens.second.amount(raw = amountOutEstimated).amount

        val difference = amountInAsReal - amountOutAsReal

        priceImpact = Percentage(
            fractionalValue = when (difference > 0.bn) {
                true -> difference / amountInAsReal
                false -> 0.bn
            }
        )
    }

    val nerveContract = networkClient
        .getNerveContract(crossChain.stablePool)
    fun callData(deadline: BigInt) = nerveContract
        .getSwapCallData(tokenIndexFrom, tokenIndexTo, dx = amountIn, minDy = 0.bi, deadline)

    suspend fun execute(
        credentials: Credentials,
        deadline: BigInt,
        gasProvider: GasProvider? = null
    ) = nerveContract.swap(
        credentials = credentials,
        tokenIndexFrom = tokenIndexFrom,
        tokenIndexTo = tokenIndexTo,
        dx = amountIn,
        minDy = 0.bi,
        deadline = deadline,
        gasProvider = gasProvider
    )
}
