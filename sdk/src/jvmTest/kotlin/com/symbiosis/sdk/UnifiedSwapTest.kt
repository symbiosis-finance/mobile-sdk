package com.symbiosis.sdk

import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.currency.amount
import com.symbiosis.sdk.swap.unified.UnifiedSwapRepository
import com.symbiosis.sdk.swap.unified.UnifiedSwapTrade
import com.symbiosis.sdk.swap.unified.UnifiedSwapTransaction
import kotlinx.coroutines.runBlocking
import org.junit.Test

class UnifiedSwapTest {
    @Test
    fun crossChainTest() {
        runBlocking {

            // first, we select two tokens to make trade against

            val inputToken = testSdk.bscTestnet.token.BNB
            val outputToken = testSdk.polygonMumbai.token.MATIC

            // then, we should check if the networks is supported by symbiosis

            when (testSdk.swap.isChainsSupported(inputToken.network, outputToken.network)) {
                true -> println("Symbiosis supports trading ${inputToken.network} -> ${outputToken.network}")
                false -> return@runBlocking println("Cannot trade against selected networks (${inputToken.network} -> ${outputToken.network})")
            }

            // after, we should check limits to trade

            when (val range = testSdk.swap.getAllowedRangeForInputToken(inputToken, outputToken.network)) {
                is UnifiedSwapRepository.AllowedRangeResult.Limited ->
                    println("You can trade from ${range.min.amount} to ${range.max.amount} of input token ($inputToken)")
                is UnifiedSwapRepository.AllowedRangeResult.NoLimit ->
                    println("No limits for current trade set")
            }

            println()

            // finally, the trade

            val amountIn = inputToken.amount(0.01.bn)

            println("Trading ${amountIn.amount} of $inputToken to $outputToken")

            val tradeResult = testSdk.swap.findBestTrade(
                amountIn = amountIn.raw,
                tokens = TokenPair(
                    first = inputToken,
                    second = outputToken
                ),
                from = alexWalletAddress
            )

            // handling the results

            // there is a bunch of possible errors you should check for
            val trade = when (tradeResult) {
                is UnifiedSwapRepository.SwapResult.StableTokensGreaterThanMax ->
                    return@runBlocking println("Can't swap ${amountIn.amount} ($${tradeResult.actualInDollars}), max allowed is $${tradeResult.maxInDollars}")
                is UnifiedSwapRepository.SwapResult.StableTokensLessThanBridgingFee ->
                    return@runBlocking println("Can't swap ${amountIn.amount} ($${tradeResult.actualInDollars}), because bridging fee is higher ($${tradeResult.bridgingFee.amount})")
                is UnifiedSwapRepository.SwapResult.StableTokensLessThanMin ->
                    return@runBlocking println("Can't swap ${amountIn.amount} ($${tradeResult.actualInDollars}), min allowed is $${tradeResult.minInDollars} ")
                is UnifiedSwapRepository.SwapResult.TradeNotFound ->
                    return@runBlocking println("Trade was not found for selected tokens ($inputToken -> $outputToken)")
                is UnifiedSwapRepository.SwapResult.Success ->
                    tradeResult.trade
            }

            println("Trade successfully found")
            println("Amount out estimated: ${trade.amountOutEstimated.amount} of $outputToken")
            println("Amount out min: ${trade.amountOutMin.amount} of $outputToken")
            println()
            println("Executing...")

            // executing the trade

            val transaction = when (val result = trade.execute(alexCredentials)) {
                // we don't want to throw errors, so the explicit check for this is required
                is UnifiedSwapTrade.ExecuteResult.ExecutionRevertedWithoutSending ->
                    return@runBlocking println("Transaction reverted with unknown error")
                is UnifiedSwapTrade.ExecuteResult.Success ->
                    result.transaction
            }

            // waiting for completion

            when (val result = transaction.waitForCompletion()) {
                is UnifiedSwapTransaction.CompletionResult.Success ->
                    println("Trade successfully executed! Tx hash on input network: ${result.receipt.txHash}")
                is UnifiedSwapTransaction.CompletionResult.TransactionFailed ->
                    println("Unknown exception occurred during execution. Tx hash on input network: ${result.receipt.txHash}")
            }
        }
    }
}

