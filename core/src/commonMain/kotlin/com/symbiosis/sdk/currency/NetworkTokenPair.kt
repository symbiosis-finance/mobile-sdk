package com.symbiosis.sdk.currency

import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.entity.ContractAddress

fun NetworkTokenPair(first: DecimalsToken, second: DecimalsToken): NetworkTokenPair =
    _NetworkTokenPair(first, second)

@Suppress("ClassName")
private data class _NetworkTokenPair(
    override val first: DecimalsToken,
    override val second: DecimalsToken
) : NetworkTokenPair, TokenPair by TokenPair(first, second) {
    init {
        require(first.asToken.network.chainId == second.asToken.network.chainId)
    }
}

/**
 * Currencies in this pair should be from one network
 */
interface NetworkTokenPair : TokenPair {
    val nativeTokensCount get() = listOf(first, second).count { it is NativeToken }
    val network: Network get() = first.asToken.network

    val thisOrWrapped: Erc20Only get() = Erc20Only(
        first = first.thisOrWrapped,
        second = second.thisOrWrapped
    )

    val addressList: List<ContractAddress> get() {
        return listOf(first, second).map { it.thisOrWrapped.tokenAddress }
    }


    companion object {
        fun Erc20Only(first: DecimalsErc20Token, second: DecimalsErc20Token): Erc20Only =
            _Erc20NetworkTokenPair(first, second)
    }

    interface Erc20Only : NetworkTokenPair {
        override val first: DecimalsErc20Token
        override val second: DecimalsErc20Token

        override fun asList(): List<DecimalsErc20Token> = listOf(first, second)
    }
}

@Suppress("ClassName")
private data class _Erc20NetworkTokenPair(
    override val first: DecimalsErc20Token,
    override val second: DecimalsErc20Token
) : NetworkTokenPair.Erc20Only, NetworkTokenPair by NetworkTokenPair(first, second) {
    override fun asList(): List<DecimalsErc20Token> = listOf(first, second)
}
