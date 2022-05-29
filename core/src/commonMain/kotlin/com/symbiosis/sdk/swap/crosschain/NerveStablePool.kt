package com.symbiosis.sdk.swap.crosschain

import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.network.contract.SynthFabricContract
import com.symbiosis.sdk.network.contract.getSyntheticToken
import dev.icerock.moko.web3.ContractAddress

data class NerveStablePool(
    val address: ContractAddress,
    val fromToken: DecimalsErc20Token,
    val targetToken: DecimalsErc20Token
) {
    val fromNetwork: Network = fromToken.network
    val targetNetwork: Network = targetToken.network
    val tokens: List<Erc20Token> get() = listOf(fromToken, targetToken)

    suspend fun getTargetTokenSynth(synthFabric: SynthFabricContract): Erc20Token {
        require(synthFabric.network.chainId == fromToken.network.chainId) { "Invalid network client provided" }
        return synthFabric.getSyntheticToken(targetToken) ?:
            error("Invalid pool configuration: target token has no synthetic")
    }

    suspend fun getPoolRoute(client: SynthFabricContract): NetworkTokenPair.Erc20Only =
        NetworkTokenPair.Erc20Only(
            first = fromToken,
            second = getTargetTokenSynth(client)
        )
}

/**
 * Automatically selects the right synth factory to avoid selection errors
 */
suspend fun NerveStablePool.getTargetTokenSynth(fromFabric: SynthFabricContract, targetFabric: SynthFabricContract): Erc20Token {
    require(
        setOf(fromFabric.network.chainId.toInt(), targetFabric.network.chainId.toInt()) ==
                setOf(fromNetwork.chainId.toInt(), targetNetwork.chainId.toInt())
    ) {
        "Given synth fabrics does not match the pool networks"
    }

    return when (fromFabric.network.chainId) {
        fromNetwork.chainId -> getTargetTokenSynth(fromFabric)
        else -> getTargetTokenSynth(targetFabric)
    }
}
