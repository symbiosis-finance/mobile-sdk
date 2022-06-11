package com.symbiosis.sdk.swap.singleNetwork

import com.symbiosis.sdk.swap.oneInch.OneInchSwapRepository
import com.symbiosis.sdk.swap.singleNetwork.adapter.OneInchSwapAdapter
import com.symbiosis.sdk.swap.singleNetwork.adapter.UniLikeSwapAdapter
import com.symbiosis.sdk.swap.uni.UniLikeSwapRepository

fun SingleNetworkSwapRepository(
    uniLike: UniLikeSwapRepository,
    oneInchIfSupported: OneInchSwapRepository?
): SingleNetworkSwapRepository {
    val uniLikeAdapter = UniLikeSwapAdapter(uniLike)

    val oneInchIfSupportedAdapter = oneInchIfSupported
        ?.let(::OneInchSwapAdapter)

    return SingleNetworkSwapRepository(
        listOfNotNull(uniLikeAdapter, oneInchIfSupportedAdapter)
    )
}
