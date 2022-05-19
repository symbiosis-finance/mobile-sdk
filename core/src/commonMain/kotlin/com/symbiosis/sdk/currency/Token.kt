package com.symbiosis.sdk.currency

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.ContractAddress

sealed interface Token {
    val network: Network
}
val Token.thisOrWrapped: Erc20Token get() = when (this) {
    is Erc20Token -> this
    is NativeToken -> wrapped
}

fun Erc20Token(network: Network, tokenAddress: ContractAddress): Erc20Token =
    object : Erc20Token {
        override val network = network
        override val tokenAddress = tokenAddress

        override fun equals(other: Any?): Boolean {
            return other is Erc20Token &&
                    other.network.chainId == network.chainId &&
                    other.tokenAddress.prefixed == tokenAddress.prefixed
        }
    }

// For custom tokens
interface Erc20Token : Token {
    val tokenAddress: ContractAddress
}

fun NativeToken(wrapped: Erc20Token, network: Network = wrapped.network): NativeToken =
    object : NativeToken {
        override val wrapped: Erc20Token = wrapped
        override val network: Network = network

        override fun equals(other: Any?): Boolean {
            return other is NativeToken &&
                    other.network.chainId == network.chainId
        }
    }

// For eth, bnb, etc
interface NativeToken : Token {
    val wrapped: Erc20Token
}


sealed interface DecimalsToken {
    val decimals: Int
}

interface DecimalsErc20Token : DecimalsToken, Erc20Token
fun DecimalsErc20Token(network: Network, tokenAddress: ContractAddress, decimals: Int) = object : DecimalsErc20Token {
    override val network: Network = network
    override val tokenAddress: ContractAddress = tokenAddress
    override val decimals: Int = decimals

    override fun equals(other: Any?): Boolean {
        return other is Erc20Token &&
                other.network.chainId == network.chainId &&
                other.tokenAddress.prefixed == tokenAddress.prefixed
    }
}

interface DecimalsNativeToken : DecimalsToken, NativeToken
fun DecimalsNativeToken(wrapped: Erc20Token, network: Network = wrapped.network, decimals: Int = 18) = object : DecimalsNativeToken {
    override val network: Network = network
    override val wrapped: Erc20Token = wrapped
    override val decimals: Int = decimals

    override fun equals(other: Any?): Boolean {
        return other is NativeToken &&
                other.network.chainId == network.chainId
    }
}

fun DecimalsToken.convertIntegerToReal(integer: BigInt) =
    TokenAmount(integer, decimals).amount
fun DecimalsToken.convertRealToInteger(real: BigNum) =
    TokenAmount(real, decimals).raw
