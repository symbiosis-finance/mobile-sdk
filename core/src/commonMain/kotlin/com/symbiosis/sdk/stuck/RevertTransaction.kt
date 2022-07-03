package com.symbiosis.sdk.stuck

import dev.icerock.moko.web3.entity.TransactionHash
import dev.icerock.moko.web3.entity.TransactionReceipt
import dev.icerock.moko.web3.requests.waitForTransactionReceipt

class RevertTransaction(
    val request: StuckTransaction,
    val transactionHash: TransactionHash
) {
    suspend fun waitForCompletion(): TransactionReceipt =
        request.targetClient.waitForTransactionReceipt(transactionHash)
}
