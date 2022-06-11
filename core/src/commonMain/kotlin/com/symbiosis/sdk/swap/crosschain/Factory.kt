package com.symbiosis.sdk.swap.crosschain

import com.symbiosis.sdk.swap.crosschain.bridging.DefaultBridgingFeeProvider
import com.symbiosis.sdk.swap.crosschain.nerve.NerveSwapRepository
import com.symbiosis.sdk.symbiosisClient

fun CrossChainSwapRepository(
    crossChain: CrossChain
): CrossChainSwapRepository {
    val inputNetworkClient = crossChain.fromNetwork.symbiosisClient
    val outputNetworkClient = crossChain.toNetwork.symbiosisClient

    val inputSingleNetworkSwap = DefaultSingleNetworkSwapRepositoryAdapter(inputNetworkClient.swap)

    val stable = DefaultStableSwapRepositoryAdapter(
        NerveSwapRepository(crossChain),
        crossChain.fromNetwork.swapTTLProvider,
        crossChain.fromNetwork.chainId
    )

    val outputSingleNetworkSwap = DefaultSingleNetworkSwapRepositoryAdapter(outputNetworkClient.swap)

    val adapter = DefaultCrossChainAdapter(
        crossChain = crossChain,
        inputSingleNetworkSwap = inputSingleNetworkSwap,
        stable = stable,
        outputSingleNetworkSwap = outputSingleNetworkSwap,
        bridgingFeeProvider = DefaultBridgingFeeProvider()
    )

    return CrossChainSwapRepository(adapter)
}
