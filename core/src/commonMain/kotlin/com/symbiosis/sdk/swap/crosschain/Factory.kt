package com.symbiosis.sdk.swap.crosschain

import com.symbiosis.sdk.ClientsManager
import com.symbiosis.sdk.swap.crosschain.bridging.DefaultBridgingFeeProvider
import com.symbiosis.sdk.swap.crosschain.nerve.NerveSwapRepository

fun CrossChainSwapRepository(
    crossChain: CrossChain
): CrossChainSwapRepository {
    val inputNetworkClient = ClientsManager.getNetworkClient(crossChain.fromNetwork)
    val outputNetworkClient = ClientsManager.getNetworkClient(crossChain.toNetwork)

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
