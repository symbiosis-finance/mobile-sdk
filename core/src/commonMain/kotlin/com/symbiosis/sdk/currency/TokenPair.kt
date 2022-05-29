package com.symbiosis.sdk.currency

fun TokenPair(first: Token, second: Token): TokenPair = _TokenPair(first, second)

@Suppress("ClassName")
private data class _TokenPair(override val first: Token, override val second: Token): TokenPair {
    init {
        require(first != second)
    }
}

interface TokenPair {
    val first: Token
    val second: Token

    fun asNetworkTokenPair(): NetworkTokenPair? =
        when (first.network.chainId == second.network.chainId) {
            true -> NetworkTokenPair(first, second)
            false -> null
        }

    fun requireNetworkTokenPair() = asNetworkTokenPair()!!

    fun asList() = listOf(first, second)
}
