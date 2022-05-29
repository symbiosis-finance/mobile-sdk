package com.symbiosis.sdk.swap.uni

import com.symbiosis.sdk.network.NetworkClient
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.requests.waitForTransactionReceipt

class UniLikeSwapTransaction(
    val networkClient: NetworkClient,
    val transactionHash: TransactionHash
) {
    // TODO: Check for status, may be failure if InsufficientLiquidity for amountOutMin/amountInMax
    suspend fun waitForReceipt() = networkClient.waitForTransactionReceipt(transactionHash)
}
