package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.crosschain.nerve.NerveSwapTrade
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.hex.HexString

sealed interface StableSwapTradeAdapter {
    val amountIn: BigInt
    val amountOutEstimated: BigInt
    val priceImpact: Percentage
    val tokens: StableSwapTokenPairAdapter
    val routerAddress: ContractAddress
    val route: NetworkTokenPair.Erc20Only
    val synthToken: DecimalsErc20Token


    suspend fun callData(deadline: BigInt?): HexString

    data class Default(
        val underlying: NerveSwapTrade,
        override val synthToken: DecimalsErc20Token,
        override val route: NetworkTokenPair.Erc20Only,
        private val defaultDeadlineProvider: suspend () -> BigInt
    ) : StableSwapTradeAdapter {
        override val amountIn: BigInt = underlying.amountIn
        override val amountOutEstimated: BigInt = underlying.amountOutEstimated
        override val priceImpact: Percentage = underlying.priceImpact
        override val tokens: StableSwapTokenPairAdapter = underlying.tokens.asStableSwap
        override suspend fun callData(deadline: BigInt?): HexString = underlying
            .callData(deadline = deadline ?: defaultDeadlineProvider())
        override val routerAddress: ContractAddress = underlying.crossChain.stablePool.address
    }
}
