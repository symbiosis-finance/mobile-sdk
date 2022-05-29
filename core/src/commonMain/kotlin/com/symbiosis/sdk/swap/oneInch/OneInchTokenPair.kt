package com.symbiosis.sdk.swap.oneInch

import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.network.Network

class OneInchTokenPair(val first: OneInchToken, val second: OneInchToken) {
    init {
        require(first.address != second.address)
    }
}

val NetworkTokenPair.asOneInchPair get() = OneInchTokenPair(first.asOneInchToken, second.asOneInchToken)

fun OneInchTokenPair.asNetworkPair(network: Network) =
    NetworkTokenPair(first.asToken(network), second.asToken(network))
