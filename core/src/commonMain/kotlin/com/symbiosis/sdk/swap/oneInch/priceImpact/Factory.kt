package com.symbiosis.sdk.swap.oneInch.priceImpact

import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.swap.oneInch.OneInchSwapRepository

fun OneInchPriceImpactRepository(
    networkClient: NetworkClient,
    network: OneInchSwapRepository.Network
): OneInchPriceImpactRepository {
    val adapter = DefaultOneInchPriceImpactAdapter(networkClient, networkClient.oneInchOracle(network.oracleAddress))
    return OneInchPriceImpactRepository(adapter)
}
