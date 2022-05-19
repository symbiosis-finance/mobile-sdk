package com.symbiosis.sdk.swap.uni

import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.swap.PendingTransaction
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.entity.TransactionReceipt
import dev.icerock.moko.web3.requests.waitForTransactionReceipt

class Web3UniLikePendingTransaction(
    private val networkClient: NetworkClient,
    private val transactionHash: TransactionHash
) : PendingTransaction {
    suspend fun waitForReceipt(
        timeOutMillis: Long? = 1L * 60L * 1_000L,
        intervalMillis: Long = 1_000
    ): TransactionReceipt = networkClient
        .waitForTransactionReceipt(transactionHash, timeOutMillis, intervalMillis)

    override suspend fun wait() {
        waitForReceipt()
    }
}