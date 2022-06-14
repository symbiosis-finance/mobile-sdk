package com.symbiosis.sdk.swap.crosschain.bridging

import com.symbiosis.sdk.swap.crosschain.CrossChain

fun DefaultBridgingFeeProvider(): DefaultBridgingFeeProvider {
    val defaultFactory = object : DefaultBridgingFeeProvider.Adapter.Factory {
        override fun create(crossChain: CrossChain): DefaultBridgingFeeProvider.Adapter =
            when (crossChain.hasPoolOnFirstNetwork) {
                true -> BurnBridgingFeeProviderAdapter(crossChain)
                false -> SynthBridgingFeeProviderAdapter(crossChain)
            }
    }

    return DefaultBridgingFeeProvider(defaultFactory)
}
