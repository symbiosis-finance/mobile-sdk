package com.symbiosis.sdk.swap.singleNetwork

import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.swap.oneInch.OneInchTokenPair
import com.symbiosis.sdk.swap.oneInch.asOneInchToken

class SingleNetworkTokenPair(val first: DecimalsToken, val second: DecimalsToken) {
    init {
        require(first.asToken.network.chainId == second.asToken.network.chainId)
        require(first != second)
    }

    val network = first.asToken.network
}

val SingleNetworkTokenPair.asOneInchPair get() = OneInchTokenPair(first.asOneInchToken, second.asOneInchToken)
val SingleNetworkTokenPair.asNetworkTokenPair get() = NetworkTokenPair(first, second)
