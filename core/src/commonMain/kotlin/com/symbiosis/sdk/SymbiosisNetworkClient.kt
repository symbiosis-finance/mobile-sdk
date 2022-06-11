package com.symbiosis.sdk

import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.swap.oneInch.OneInchSwapRepository
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkSwapRepository
import com.symbiosis.sdk.swap.uni.UniLikeSwapRepository

/**
 * Everything for swaps inside one network
 */
class SymbiosisNetworkClient(val networkClient: NetworkClient) {
    val uniLike: UniLikeSwapRepository = UniLikeSwapRepository(networkClient)
    val oneInchIfSupported: OneInchSwapRepository? = OneInchSwapRepository(networkClient)
    val swap: SingleNetworkSwapRepository = SingleNetworkSwapRepository(uniLike, oneInchIfSupported)
}

val Network.symbiosisClient get() = SymbiosisNetworkClient(NetworkClient(network = this))
