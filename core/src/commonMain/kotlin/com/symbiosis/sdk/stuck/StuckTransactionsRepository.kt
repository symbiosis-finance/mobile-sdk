package com.symbiosis.sdk.stuck

import com.symbiosis.sdk.SymbiosisNetworkClient
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.network.contract.OutboundRequest
import dev.icerock.moko.web3.entity.BlockState
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.entity.WalletAddress
import dev.icerock.moko.web3.entity.Web3RpcRequest
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.requests.executeBatch
import dev.icerock.moko.web3.requests.getBlockNumber

class StuckTransactionsRepository(
    mainClient: SymbiosisNetworkClient,
    private val advisorUrl: String,
    private val adapter: Adapter
) {
    private val networkClient = mainClient.networkClient

    suspend fun getStuckTransactions(address: WalletAddress): List<StuckTransaction> {
        // get inbound requests

        val blockNumber = networkClient.getBlockNumber()
        val fromBlock = blockNumber - networkClient.network.maxBlocksPerRequest

        val requests = networkClient.executeBatch(
            networkClient.synthesize.getBurnRequestsRequest(
                address = address,
                fromBlock = BlockState.Quantity(fromBlock),
                toBlock = BlockState.Quantity(blockNumber)
            ),
            networkClient.portal.getSynthesizeRequestsRequest(
                address = address,
                fromBlock = BlockState.Quantity(fromBlock),
                toBlock = BlockState.Quantity(blockNumber)
            )
        ).let { (burn, synthesize) ->
            burn.mapNotNull { adapter.decodeBurnRequest(it, address) } +
                    synthesize.mapNotNull { adapter.decodeSynthRequest(it, address) }
        }

        if (requests.isEmpty()) return emptyList()

        // get outbound requests

        val outboundRequests = networkClient.executeBatch(requests.map { it.outboundRequestRequest })

        val states = requests.groupBy { request -> request.outputClient.network.chainId }
            .entries
            .flatMap { (_, requests) ->
                val client = requests.first().outputClient
                return@flatMap client.executeBatch(requests.map { it.stateRequest })
            }

        return requests
            .zip(outboundRequests)
            .zip(states) { (request, outboundRequest), state -> Triple(request, outboundRequest, state) }
            .filter { (_, outboundRequest) -> outboundRequest.state == OutboundRequest.State.Sent }
            .filter { (_, _, state) -> state == StuckTransaction.State.Pending }
            .map { (request, outboundRequest, state) ->
                StuckTransaction(
                    internalId = request.internalId,
                    externalId = request.externalId,
                    request = outboundRequest,
                    state = state,
                    fromClient = networkClient,
                    targetClient = request.outputClient,
                    advisorUrl = advisorUrl
                )
            }
    }

    interface Adapter {
        /**
         * @return null if chainId is not supported
         */
        fun decodeBurnRequest(event: LogEvent, revertableAddress: EthereumAddress): SwapRequest?

        /**
         * @return null if chainId is not supported
         */
        fun decodeSynthRequest(event: LogEvent, revertableAddress: EthereumAddress): SwapRequest?
    }

    interface SwapRequest {
        val outputClient: NetworkClient
        val internalId: Hex32String
        val externalId: Hex32String
        val outboundRequestRequest: Web3RpcRequest<out OutboundRequest>
        val stateRequest: Web3RpcRequest<StuckTransaction.State>
    }
}
