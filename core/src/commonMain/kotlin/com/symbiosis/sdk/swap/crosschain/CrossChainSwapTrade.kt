package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.crosschain.executor.CrossChainTradeExecutorAdapter
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.signing.Credentials

data class CrossChainSwapTrade(
    val tokens: TokenPair,
    val amountIn: TokenAmount,
    val amountOutEstimated: TokenAmount,
    val amountOutMin: TokenAmount,
    val dollarsAmount: BigNum,
    val inputTrade: SingleNetworkSwapTradeAdapter,
    val stableTrade: StableSwapTradeAdapter,
    val outputTrade: SingleNetworkSwapTradeAdapter,
    val slippageTolerance: Percentage,
    val recipient: EthereumAddress,
    private val bridgingFee: TokenAmount,
    private val executor: CrossChainTradeExecutorAdapter
) {
    data class TransactionFee(
        val inputTrade: TokenAmount,
        val bridgingFee: TokenAmount,
        val outputTrade: TokenAmount
    )

    data class PriceImpact(
        val inputTade: Percentage,
        val stableTrade: Percentage,
        val outputTrade: Percentage
    ) {
        val total: Percentage = (inputTade + stableTrade + outputTrade)
            .takeIf { it < 100 } ?: Percentage(1.bn)
    }

    val fee = TransactionFee(
        inputTrade = inputTrade.fee,
        bridgingFee = bridgingFee,
        outputTrade = outputTrade.fee
    )
    val priceImpact = PriceImpact(
        inputTade = inputTrade.priceImpact,
        stableTrade = stableTrade.priceImpact,
        outputTrade = outputTrade.priceImpact
    )

    suspend fun execute(credentials: Credentials, deadline: BigInt? = null) =
        executor.execute(credentials, deadline)
}
