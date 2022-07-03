package com.symbiosis.sdk.stuck

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.network.contract.OutboundRequest
import com.symbiosis.sdk.network.contract.abi.portalContractAbi
import com.symbiosis.sdk.network.contract.abi.synthesizeContractAbi
import dev.icerock.moko.web3.contract.ABIDecoder
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.entity.Web3RpcRequest
import dev.icerock.moko.web3.hex.Hex32String

class DefaultStuckTransactionsAdapter(
    private val mainClient: NetworkClient,
    otherClients: List<NetworkClient>
) : StuckTransactionsRepository.Adapter {
    private val otherClients = (otherClients + mainClient).distinctBy { it.network.chainId }

    override fun decodeBurnRequest(event: LogEvent, revertableAddress: EthereumAddress): StuckTransactionsRepository.SwapRequest? {
        val (internalId, chainId) = ABIDecoder.decodeLogEvent(synthesizeContractAbi, event)
            .let { (internalId, _, chainId) -> Hex32String(internalId as ByteArray) to (chainId as BigInt) }

        val outputClient = otherClients.firstOrNull { it.network.chainId == chainId } ?: return null

        return BurnSwapRequest(mainClient, revertableAddress, outputClient, internalId)
    }

    override fun decodeSynthRequest(event: LogEvent, revertableAddress: EthereumAddress): StuckTransactionsRepository.SwapRequest? {
        val (internalId, chainId) = ABIDecoder.decodeLogEvent(portalContractAbi, event)
            .let { (internalId, _, chainId) -> Hex32String(internalId as ByteArray) to (chainId as BigInt) }

        val outputClient = otherClients.firstOrNull { it.network.chainId == chainId } ?: return null

        return SynthSwapRequest(mainClient, revertableAddress, outputClient, internalId)
    }
}

private class BurnSwapRequest(
    inputClient: NetworkClient,
    revertableAddress: EthereumAddress,
    override val outputClient: NetworkClient,
    override val internalId: Hex32String
) : StuckTransactionsRepository.SwapRequest {
    override val externalId: Hex32String = inputClient.portal
        .getExternalId(internalId, outputClient.network, revertableAddress)
    override val outboundRequestRequest: Web3RpcRequest<out OutboundRequest> =
        inputClient.synthesize.requestsRequest(externalId)
    override val stateRequest: Web3RpcRequest<StuckTransaction.State> =
        outputClient.portal.unsynthesizeStatesRequest(externalId)
}

private class SynthSwapRequest(
    inputClient: NetworkClient,
    revertableAddress: EthereumAddress,
    override val outputClient: NetworkClient,
    override val internalId: Hex32String
) : StuckTransactionsRepository.SwapRequest {
    override val externalId: Hex32String = inputClient.synthesize
        .getExternalId(internalId, outputClient.network, revertableAddress)
    override val outboundRequestRequest: Web3RpcRequest<out OutboundRequest> =
        inputClient.portal.requestsRequest(externalId)
    override val stateRequest: Web3RpcRequest<StuckTransaction.State> =
        outputClient.synthesize.synthesizeStatesRequest(externalId)

}
