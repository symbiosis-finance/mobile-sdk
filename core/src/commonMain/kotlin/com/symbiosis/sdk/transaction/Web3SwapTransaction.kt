package com.symbiosis.sdk.transaction

import com.symbiosis.sdk.network.NetworkClient
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.entity.TransactionReceipt
import dev.icerock.moko.web3.requests.waitForTransactionReceipt

class Web3SwapTransaction(
    val networkClient: NetworkClient,
    val transactionHash: TransactionHash
) {
    sealed interface ReceiptResult {
        class Success(val receipt: TransactionReceipt) : ReceiptResult
        class Failure(val receipt: TransactionReceipt) : ReceiptResult
    }
    suspend fun waitForReceipt(): ReceiptResult {
        val receipt = networkClient.waitForTransactionReceipt(transactionHash)

        return when (receipt.status) {
            TransactionReceipt.Status.SUCCESS -> ReceiptResult.Success(receipt)
            TransactionReceipt.Status.FAILURE -> ReceiptResult.Failure(receipt)
        }
    }
}
