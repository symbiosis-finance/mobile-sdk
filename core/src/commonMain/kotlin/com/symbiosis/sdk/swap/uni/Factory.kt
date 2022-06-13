package com.symbiosis.sdk.swap.uni

import com.symbiosis.sdk.network.NetworkClient

fun UniLikeSwapRepository(networkClient: NetworkClient): UniLikeSwapRepository {
    val router = Web3UniLikeSwapRouter(networkClient)
    return UniLikeSwapRepository(router)
}
