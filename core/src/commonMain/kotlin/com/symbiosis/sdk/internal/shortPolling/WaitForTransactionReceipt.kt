package com.symbiosis.sdk.internal.shortPolling

import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.entity.TransactionReceipt
import dev.icerock.moko.web3.requests.getTransactionReceipt
import dev.icerock.moko.web3.requests.polling.shortPollingUntilSuccess


suspend fun Web3Executor.safelyWaitForTransactionReceipt(
    transactionHash: TransactionHash,
    timeOutMillis: Long? = null,
    intervalMillis: Long = 5_000
): TransactionReceipt = shortPollingUntilSuccess(timeOutMillis, intervalMillis) {
    runCatching {
        getTransactionReceipt(transactionHash)
            ?: return@shortPollingUntilSuccess Result.failure(IllegalStateException())
    }
}
