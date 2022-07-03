package com.symbiosis.sdk.stuck

import com.symbiosis.sdk.ClientsManager
import com.symbiosis.sdk.getCrossChainClient
import com.symbiosis.sdk.network.NetworkClient
import dev.icerock.moko.web3.entity.WalletAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList

suspend fun ClientsManager.getStuckTransactions(address: WalletAddress): List<StuckTransaction> =
    getStuckTransactionsAsFlow(address).toList()

suspend fun ClientsManager.getStuckTransactionsAsFlow(address: WalletAddress): Flow<StuckTransaction> =
    flow {
        allClients.forEach { client ->
            val advisorUrl = findAdvisorUrl(client.networkClient) ?: return@forEach
            emitAll(client.getStuckTransactions(address, allClients, advisorUrl).asFlow())
        }
    }

private fun ClientsManager.findAdvisorUrl(networkClient: NetworkClient): String? {
    val crossChains = allClients.mapNotNull { otherClient ->
        getCrossChainClient(networkClient.network, otherClient.networkClient.network)
    }
    if (crossChains.isEmpty()) return null

    val firstAdvisorUrl = crossChains.first().crossChain.advisorUrl

    if (crossChains.any { it.crossChain.advisorUrl != firstAdvisorUrl })
        error("Multiple advisor url available for current configuration, but this is not supported yet")

    return firstAdvisorUrl
}
