package com.symbiosis.sdk

import com.soywiz.kbignum.bi
import com.symbiosis.sdk.swap.meta.CalculatedMetaSwapTrade
import kotlinx.coroutines.runBlocking

class StuckTransactionsTest {
    //@Test
    fun getStuckTransactions() {
        runBlocking {
            repeat(3) {
                val trade = testSdk.hecoTestnetEthRinkebyClient.findBestTradeExactIn(
                    to = denWalletAddress,
                    fromToken = testSdk.hecoTestnet.token.HT,
                    targetToken = testSdk.ethRinkeby.token.ETH,
                    amountIn = 1_000_000_000_000_000.bi,
                    bridgingFeeProvider = { _, _, _, _ -> 0.bi }
                )?.also(::println) as? CalculatedMetaSwapTrade.ExactIn ?: error("Trade not found")

                val tx = testSdk.hecoTestnetEthRinkebyClient.execute(
                    credentials = denCredentials,
                    trade = trade
                )
                println(tx.waitForReceipt())
            }

//            val requests = testSdk.getStuckTransactions(alexWalletAddress)
//            println("FOUND FOLLOWING STUCK TRANSACTIONS:")
//            println(
//                requests.mapIndexed { i, it ->
//                    "${i + 1}. Stuck transaction from ${it.fromClient.network} to" +
//                            " ${it.targetClient.network}: returns ${it.request.amount} ${it.request.tokenAddress} " +
//                            "to ${it.request.recipient}"
//                }.joinToString(separator = "\n")
//            )
//
//            val reverts = requests.map { stuck -> stuck.revert(alexCredentials) }
//            println("Them all was reverted with the following REVERT transactions:")
//            println(
//                reverts.mapIndexed { i, it ->
//                    "${i + 1}. ${it.transactionHash} at ${it.request.targetClient.network.networkName}"
//                }.joinToString(separator = "\n")
//            )
//            println("Waiting for completion of every revert transaction.")
//
//            reverts.mapIndexed { i, request ->
//                launch {
//                    val receipt = request.waitForCompletion()
//                    println("Revert transaction ${i + 1} completed. Result: ${receipt.status}")
//                }
//            }.joinAll()
//
//            println("Everything Completed.")
        }
    }
}
