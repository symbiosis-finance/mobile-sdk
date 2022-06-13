package com.symbiosis.sdk

import com.symbiosis.sdk.network.networkClient
import com.symbiosis.sdk.stuck.StuckTransaction
import com.symbiosis.sdk.stuck.getStuckTransactionsAsFlow
import dev.icerock.moko.web3.BlockState
import dev.icerock.moko.web3.requests.getBlockNumber
import dev.icerock.moko.web3.requests.getLogs
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class StuckTransactionsTest {
    @Test
    fun getStuckTransactions() {
        runBlocking {
            repeat(3) {
//                val trade = testSdk.bobaRinkebyEthRinkebyClient.findBestTradeExactIn(
//                    to = denWalletAddress,
//                    fromToken = testSdk.bobaRinkeby.token.ETH,
//                    targetToken = testSdk.ethRinkeby.token.ETH,
//                    amountIn = 1_000_000_000_000_000.bi,
//                    bridgingFeeProvider = { _, _, _, _ -> 0.bi }
//                )?.also(::println) as? CalculatedMetaSwapTrade.ExactIn ?: error("Trade not found")
//
//                val tx = testSdk.bobaRinkebyEthRinkebyClient.execute(
//                    credentials = denCredentials,
//                    trade = trade
//                )
//                println(tx.waitForReceipt())
            }

            val requests = testSdk.getStuckTransactionsAsFlow(alexWalletAddress)
                .onEach { transaction ->
                    println("Stuck transaction found:")
                    println("From ${transaction.fromClient.network} to ${transaction.targetClient.network}")
                    println("You can refund ${transaction.request.amount} of ${transaction.request.tokenAddress} token")
                    println("To wallet ${transaction.request.recipient}")
                    println()
                }.toList()

            println("All transactions found. Reverting:")

            val reverts = requests.map { stuck -> stuck.revert(alexCredentials) }
                .mapNotNull { transaction ->
                    val mapped = transaction as? StuckTransaction.RevertResult.Sent

                    if (mapped == null)
                        println("Failed to send revert transaction")

                    return@mapNotNull mapped
                }.map { it.transaction }

            println("Them all was reverted with the following REVERT transactions:")
            println(
                reverts.mapIndexed { i, it ->
                    "${i + 1}. ${it.transactionHash} at ${it.request.targetClient.network.networkName}"
                }.joinToString(separator = "\n")
            )
            println("Waiting for completion of every revert transaction.")

            reverts.mapIndexed { i, request ->
                launch {
                    val receipt = request.waitForCompletion()
                    println("Revert transaction ${i + 1} completed. Result: ${receipt.status}")
                }
            }.joinAll()

            println("Everything Completed.")
        }
    }

    @Test
    fun blocksTest() {
        runBlocking {
            val block = testBSC.networkClient.getBlockNumber()
            testBSC.networkClient.getLogs(fromBlock = BlockState.Quantity(block - 1_000), toBlock = BlockState.Latest)
        }
    }
}
