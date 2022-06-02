package com.symbiosis.sdk.swap.crosschain

class DefaultTokenPairAdapter(
    val tokens: CrossChainTokenPair,
    val crossChain: CrossChain
) : TokenPairAdapter {
    override val inputPair: SingleNetworkTokenPairAdapter? =
        when (tokens.first == crossChain.fromToken) {
            true -> null
            false -> SingleNetworkTokenPairAdapter(tokens.first, crossChain.fromToken)
        }

    override val stablePair: StableSwapTokenPairAdapter =
        StableSwapTokenPairAdapter(
            first = crossChain.fromToken,
            second = crossChain.targetToken
        )

    override val outputPair: SingleNetworkTokenPairAdapter? =
        when (tokens.second == crossChain.targetToken) {
            true -> null
            false -> SingleNetworkTokenPairAdapter(crossChain.targetToken, tokens.second)
        }
}
