package com.symbiosis.sdk.currency

import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.ContractAddress

fun NetworkTokenPair(first: Token, second: Token): NetworkTokenPair =
    _NetworkTokenPair(first, second)

@Suppress("ClassName")
private data class _NetworkTokenPair(
    override val first: Token,
    override val second: Token
) : NetworkTokenPair, TokenPair by TokenPair(first, second) {
    init {
        require(first.network.chainId == second.network.chainId)
    }
}

/**
 * Currencies in this pair should be from one network
 */
interface NetworkTokenPair : TokenPair {
    val nativeTokensCount get() = listOf(first, second).count { it is NativeToken }
    val network: Network get() = first.network

    val thisOrWrapped: Erc20Only get() = Erc20Only(
        first = first.thisOrWrapped,
        second = second.thisOrWrapped
    )

    val addressList: List<ContractAddress> get() {
        return listOf(first, second).map { it.thisOrWrapped.tokenAddress }
    }


    companion object {
        fun Erc20Only(first: Erc20Token, second: Erc20Token): Erc20Only =
            _Erc20NetworkTokenPair(first, second)
    }

    interface Erc20Only : NetworkTokenPair {
        override val first: Erc20Token
        override val second: Erc20Token

        override fun asList(): List<Erc20Token> = listOf(first, second)
    }
}

infix fun Erc20Token.networkPairWith(other: Erc20Token) =
    NetworkTokenPair.Erc20Only(first = this, second = other)

@Suppress("ClassName")
private data class _Erc20NetworkTokenPair(
    override val first: Erc20Token,
    override val second: Erc20Token
) : NetworkTokenPair.Erc20Only, NetworkTokenPair by NetworkTokenPair(first, second) {
    override fun asList(): List<Erc20Token> = listOf(first, second)
}
