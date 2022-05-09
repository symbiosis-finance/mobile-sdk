package com.symbiosis.sdk

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.crosschain.CrossChain
import com.symbiosis.sdk.crosschain.CrossChainClient
import com.symbiosis.sdk.crosschain.RawUsageOfCrossChainConstructor
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.network.RawUsageOfNetworkConstructor

/**
 * This is a parent class for every network client,
 * it's like a multi-network manager of your single-network clients
 *
 * The entity which responds for the exact network called [NetworkClient]
 *
 * @see NetworkClient
 */
@OptIn(RawUsageOfNetworkConstructor::class, RawUsageOfCrossChainConstructor::class)
interface ClientsManager {
    val allNetworks: List<Network>
    val allTokens: List<Token>
    val allClients: List<NetworkClient>
    val allCrossChainClients: List<CrossChainClient>

    fun getCrossChainClient(firstNetwork: Network, secondNetwork: Network) =
        getCrossChainClient(firstNetwork.chainId, secondNetwork.chainId)

    fun getCrossChainClient(firstNetworkChainId: BigInt, secondNetworkChainId: BigInt) = allCrossChainClients
        .find { it.crossChain.fromNetwork.chainId == firstNetworkChainId &&
                it.crossChain.toNetwork.chainId == secondNetworkChainId }

    fun getNetworkClient(network: Network) = Companion.getNetworkClient(network)
    fun getCrossChainClient(crossChain: CrossChain) = Companion.getCrossChainClient(crossChain)

    companion object {
        fun getNetworkClient(network: Network) = NetworkClient(network)
        fun getCrossChainClient(crossChain: CrossChain) = CrossChainClient(crossChain)
    }
}

fun ClientsManager() = object : ClientsManager {
    override val allNetworks: List<Network> = listOf()
    override val allTokens: List<Token> = listOf()
    override val allClients: List<NetworkClient> = listOf()
    override val allCrossChainClients: List<CrossChainClient> = listOf()
}
