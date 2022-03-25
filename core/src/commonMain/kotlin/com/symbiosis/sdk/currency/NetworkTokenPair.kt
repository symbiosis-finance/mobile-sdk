package com.symbiosis.sdk.currency

import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.ContractAddress

/**
 * Currencies in this pair should be from one network
 */
class NetworkTokenPair(
    val first: Token,
    val second: Token
) {
    val nativesTokenCount
        get() = listOf(first,second).count { it is NativeToken }

    val network: Network = first.network

    val thisOrWrapped: NetworkTokenPair get() {
        return NetworkTokenPair(
            first = first.thisOrWrapped,
            second = second.thisOrWrapped
        )
    }

    val addressList: List<ContractAddress> get() {
        return listOf(first, second).map { it.thisOrWrapped.tokenAddress }
    }
}

infix fun Erc20Token.networkPairWith(other: Erc20Token) =
    NetworkTokenPair(first = this, second = other)
