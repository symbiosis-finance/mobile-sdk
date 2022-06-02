package com.symbiosis.sdk.swap.crosschain

import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTokenPair

class CrossChainTokenPair(val first: DecimalsToken, val second: DecimalsToken) {
    init {
        require(first != second)
    }
}

class SingleNetworkTokenPairAdapter(val first: DecimalsToken, val second: DecimalsToken) {
    init {
        require(first.asToken.network.chainId == second.asToken.network.chainId)
        require(first != second)
    }

    val network = first.asToken.network

    val asSingleNetworkTokenPair get() = SingleNetworkTokenPair(first, second)
}
