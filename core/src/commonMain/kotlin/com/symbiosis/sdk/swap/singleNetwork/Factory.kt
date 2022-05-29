package com.symbiosis.sdk.swap.singleNetwork

import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.swap.singleNetwork.adapter.OneInchSwapAdapter
import com.symbiosis.sdk.swap.singleNetwork.adapter.UniLikeSwapAdapter

fun SingleNetworkSwapRepository(networkClient: NetworkClient): SingleNetworkSwapRepository {
    val uniLike = UniLikeSwapAdapter(networkClient.uniLike)

    val oneInch = networkClient
        .oneInchIfSupported
        ?.let(::OneInchSwapAdapter)

    return SingleNetworkSwapRepository(
        listOfNotNull(uniLike, oneInch)
    )
}
