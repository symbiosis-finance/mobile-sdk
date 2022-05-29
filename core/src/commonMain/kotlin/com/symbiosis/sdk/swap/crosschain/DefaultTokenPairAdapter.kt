package com.symbiosis.sdk.swap.crosschain

import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.TokenPair

class DefaultTokenPairAdapter(
    val tokens: TokenPair,
    val crossChain: CrossChain
) : TokenPairAdapter {
    override val inputPair: NetworkTokenPair? =
        when (tokens.first == crossChain.fromToken) {
            true -> null
            false -> NetworkTokenPair(tokens.first, crossChain.fromToken)
        }

    override val stablePair: StableSwapTokenPair =
        StableSwapTokenPair(
            first = crossChain.fromToken,
            second = crossChain.targetToken
        )

    override val outputPair: NetworkTokenPair? =
        when (tokens.second == crossChain.targetToken) {
            true -> null
            false -> NetworkTokenPair(crossChain.targetToken, tokens.second)
        }
}
