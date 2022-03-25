package com.symbiosis.sdk

import com.symbiosis.sdk.crosschain.CrossChain
import com.symbiosis.sdk.crosschain.CrossChainClient
import com.symbiosis.sdk.crosschain.RawUsageOfCrossChainConstructor
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
open class ClientsManager {
    fun getNetworkClient(network: Network) = NetworkClient(network)
    fun getCrossChainClient(crossChain: CrossChain) = CrossChainClient(crossChain)

    companion object : ClientsManager()
}
