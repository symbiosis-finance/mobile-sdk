package com.symbiosis.sdk.swap.oneInch

import com.symbiosis.sdk.network.NetworkClient

fun OneInchSwapRepository(networkClient: NetworkClient): OneInchSwapRepository? {
    return OneInchSwapRepository(
        router = DefaultHttpRouter(networkClient),
        network = OneInchSwapRepository.Network
            .values()
            .firstOrNull { it.chainId == networkClient.network.chainId }
            ?: return null
    )
}
