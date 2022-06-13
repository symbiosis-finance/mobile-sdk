package com.symbiosis.sdk.swap.crosschain

import com.symbiosis.sdk.currency.NetworkTokenPair

interface TokenPairAdapter {
    val inputPair: NetworkTokenPair?
    val stablePair: StableSwapTokenPairAdapter
    val outputPair: NetworkTokenPair?
}
