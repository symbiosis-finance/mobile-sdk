package com.symbiosis.sdk.swap.oneInch

import com.symbiosis.sdk.network.NetworkClient

fun OneInchSwapRepository(networkClient: NetworkClient): OneInchSwapRepository? {
    val network = OneInchSwapRepository.Network
        .values()
        .firstOrNull { it.chainId == networkClient.network.chainId }
        ?: return null

    return OneInchSwapRepository(
        router = DefaultHttpRouter(networkClient, network),
        network = network
    )
}
