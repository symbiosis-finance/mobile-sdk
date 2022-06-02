package com.symbiosis.sdk.swap.crosschain

interface TokenPairAdapter {
    val inputPair: SingleNetworkTokenPairAdapter?
    val stablePair: StableSwapTokenPairAdapter
    val outputPair: SingleNetworkTokenPairAdapter?
}
