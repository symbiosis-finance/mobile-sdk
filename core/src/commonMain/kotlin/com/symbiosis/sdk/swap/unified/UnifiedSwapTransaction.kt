package com.symbiosis.sdk.swap.unified

import com.symbiosis.sdk.swap.crosschain.transaction.CrossChainSwapTransaction
import com.symbiosis.sdk.transaction.Web3SwapTransaction
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.entity.TransactionReceipt


sealed interface UnifiedSwapTransaction {
    val transactionHash: TransactionHash

    sealed interface ReceiptResult {
        class Success(val receipt: TransactionReceipt) : ReceiptResult
        class Failure(val receipt: TransactionReceipt) : ReceiptResult
    }

    suspend fun waitForTransactionReceipt(): ReceiptResult

    sealed interface CompletionResult {
        class TransactionFailed(val receipt: TransactionReceipt) : CompletionResult
        class Success(val receipt: TransactionReceipt) : CompletionResult
    }

    suspend fun waitForCompletion(): CompletionResult

    interface SingleNetwork : UnifiedSwapTransaction {
        override suspend fun waitForCompletion(): CompletionResult =
            when (val result = waitForTransactionReceipt()) {
                is ReceiptResult.Failure -> CompletionResult.TransactionFailed(result.receipt)
                is ReceiptResult.Success -> CompletionResult.Success(result.receipt)
            }

        class Default(val underlying: Web3SwapTransaction) : SingleNetwork {
            override val transactionHash = underlying.transactionHash
            override suspend fun waitForTransactionReceipt(): ReceiptResult =
                when (val result = underlying.waitForReceipt()) {
                    is Web3SwapTransaction.ReceiptResult.Failure -> ReceiptResult.Failure(result.receipt)
                    is Web3SwapTransaction.ReceiptResult.Success -> ReceiptResult.Success(result.receipt)
                }
        }
    }

    interface CrossChain : UnifiedSwapTransaction {
        suspend fun waitForCompletionEvent(transactionReceipt: TransactionReceipt): LogEvent

        override suspend fun waitForCompletion(): CompletionResult {
            val receipt = when (val result = waitForTransactionReceipt()) {
                is ReceiptResult.Failure -> return CompletionResult.TransactionFailed(result.receipt)
                is ReceiptResult.Success -> result.receipt
            }

            waitForCompletionEvent(receipt)

            return CompletionResult.Success(receipt)
        }

        class Default(val underlying: CrossChainSwapTransaction) : CrossChain {
            override val transactionHash = underlying.transactionHash
            override suspend fun waitForCompletionEvent(transactionReceipt: TransactionReceipt): LogEvent =
                underlying.waitForCompletionEvent(transactionReceipt)

            override suspend fun waitForTransactionReceipt(): ReceiptResult {
                val receipt = underlying.waitForReceiptOnInputNetwork()

                return when (receipt.status) {
                    TransactionReceipt.Status.SUCCESS -> ReceiptResult.Success(receipt)
                    TransactionReceipt.Status.FAILURE -> ReceiptResult.Failure(receipt)
                }
            }
        }
    }
}
