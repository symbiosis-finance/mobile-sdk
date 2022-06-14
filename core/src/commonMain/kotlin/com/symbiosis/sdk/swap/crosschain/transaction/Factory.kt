package com.symbiosis.sdk.swap.crosschain.transaction

import com.symbiosis.sdk.network.networkClient
import com.symbiosis.sdk.swap.crosschain.CrossChain
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.TransactionHash

fun CrossChainSwapTransaction(
    transactionHash: TransactionHash,
    revertableAddress: EthereumAddress,
    crossChain: CrossChain
): CrossChainSwapTransaction {
    val inputNetworkClient = crossChain.fromNetwork.networkClient
    val outputNetworkClient = crossChain.toNetwork.networkClient

    return when (crossChain.hasPoolOnFirstNetwork) {
        true -> BurnCrossChainTransactionAdapter(outputNetworkClient)
        false -> SynthCrossChainTransactionAdapter(outputNetworkClient)
    }.let { adapter ->
        CrossChainSwapTransaction(transactionHash, inputNetworkClient, outputNetworkClient, revertableAddress, adapter)
    }
}
