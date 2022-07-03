package com.symbiosis.sdk.swap.crosschain.transaction

import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.network.contract.requireBurnRequestEvent
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.entity.TransactionReceipt
import dev.icerock.moko.web3.hex.Hex32String

class BurnCrossChainTransactionAdapter(
    private val outputNetworkClient: NetworkClient
) : CrossChainSwapTransaction.Adapter {
    override fun extractRequestId(receipt: TransactionReceipt): Hex32String =
        receipt.logs
            .requireBurnRequestEvent()
            .deserializeData { data -> data[0] }

    override fun eventTopics(requestId: Hex32String, revertableAddress: EthereumAddress): List<Hex32String> =
        outputNetworkClient.portal.burnCompletedEventFilter(requestId, outputNetworkClient.network, revertableAddress)

    override val eventEmitterAddress = outputNetworkClient.portal.address
}
