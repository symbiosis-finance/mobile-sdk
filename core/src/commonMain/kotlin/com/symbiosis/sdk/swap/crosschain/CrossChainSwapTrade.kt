package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.crosschain.executor.CrossChainTradeExecutorAdapter
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.EthereumAddress

data class CrossChainSwapTrade(
    val amountIn: BigInt,
    val amountOutEstimated: BigInt,
    val amountOutMin: BigInt,
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

    suspend fun execute(credentials: Credentials, deadline: BigInt? = null, gasProvider: GasProvider? = null) =
        executor.execute(credentials, deadline, gasProvider)
}
