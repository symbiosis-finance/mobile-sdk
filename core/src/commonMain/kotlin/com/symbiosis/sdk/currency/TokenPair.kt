package com.symbiosis.sdk.currency

fun TokenPair(first: DecimalsToken, second: DecimalsToken): TokenPair = _TokenPair(first, second)

@Suppress("ClassName")
private data class _TokenPair(override val first: DecimalsToken, override val second: DecimalsToken): TokenPair {
    init {
        require(first != second)
    }
}

interface TokenPair {
    val first: DecimalsToken
    val second: DecimalsToken

    fun asNetworkTokenPair(): NetworkTokenPair? =
        when (first.asToken.network.chainId == second.asToken.network.chainId) {
            true -> NetworkTokenPair(first, second)
            false -> null
        }

    fun requireNetworkTokenPair() = asNetworkTokenPair()!!

    fun asList() = listOf(first, second)
}
