package com.symbiosis.sdk

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.swap.crosschain.SymbiosisCrossChainClient

/**
 * This is a parent class for every network client,
 * it's like a multi-network manager of your single-network clients
 *
 * The entity which responds for the exact network called [NetworkClient]
 *
 * @see NetworkClient
 */
interface ClientsManager {
    val allNetworks: List<Network>
    val allTokens: List<DecimalsToken>
    val allClients: List<SymbiosisNetworkClient>
    val allCrossChainClients: List<SymbiosisCrossChainClient>
}

fun ClientsManager.findBestTrade(amountInt: BigInt): Unit = TODO()

fun ClientsManager.getCrossChainClient(firstNetwork: Network, secondNetwork: Network) =
    getCrossChainClient(firstNetwork.chainId, secondNetwork.chainId)

fun ClientsManager.getCrossChainClient(firstNetworkChainId: BigInt, secondNetworkChainId: BigInt) = allCrossChainClients
    .find { it.crossChain.fromNetwork.chainId == firstNetworkChainId &&
            it.crossChain.toNetwork.chainId == secondNetworkChainId }
