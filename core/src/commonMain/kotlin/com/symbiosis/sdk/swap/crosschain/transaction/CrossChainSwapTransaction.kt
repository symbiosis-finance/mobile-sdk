package com.symbiosis.sdk.swap.crosschain.transaction

import com.symbiosis.sdk.network.NetworkClient
import dev.icerock.moko.web3.BlockState
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.entity.TransactionReceipt
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.requests.polling.newLogsShortPolling
import dev.icerock.moko.web3.requests.waitForTransactionReceipt
import kotlinx.coroutines.flow.first

class CrossChainSwapTransaction(
    val transactionHash: TransactionHash,
    val inputNetworkClient: NetworkClient,
    val outputNetworkClient: NetworkClient,
    val revertableAddress: EthereumAddress,
    private val adapter: Adapter
) {
    sealed interface CompletionResult {
        class TransactionFailedOnInputNetwork(val receipt: TransactionReceipt) : CompletionResult
        class Success(val log: LogEvent) : CompletionResult
    }

    suspend fun waitForCompletion(): CompletionResult {
        val receipt = waitForReceiptOnInputNetwork()

        if (receipt.status != TransactionReceipt.Status.SUCCESS)
            return CompletionResult.TransactionFailedOnInputNetwork(receipt)

        return CompletionResult.Success(waitForCompletionEvent(receipt))
    }

    suspend fun waitForReceiptOnInputNetwork(): TransactionReceipt =
        inputNetworkClient.waitForTransactionReceipt(transactionHash)

    /**
     * Assume that [receipt] is SUCCESS
     */
    suspend fun waitForCompletionEvent(receipt: TransactionReceipt): LogEvent {
        require(receipt.status == TransactionReceipt.Status.SUCCESS)

        val requestId = adapter.extractRequestId(receipt)
        return waitForCompletionEvent(requestId)
    }

    suspend fun waitForCompletionEvent(requestId: Hex32String): LogEvent {
        return outputNetworkClient.newLogsShortPolling(
            fromBlock = BlockState.Earliest,
            topics = adapter.eventTopics(requestId, revertableAddress),
            address = adapter.eventEmitterAddress
        ).first()
    }

    interface Adapter {
        fun extractRequestId(receipt: TransactionReceipt): Hex32String
        fun eventTopics(requestId: Hex32String, revertableAddress: EthereumAddress): List<Hex32String>
        val eventEmitterAddress: ContractAddress
    }
}
