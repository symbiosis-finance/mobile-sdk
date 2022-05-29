package com.symbiosis.sdk.swap.oneInch

import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.NativeToken
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.ContractAddress

class OneInchToken(val address: ContractAddress) {
    companion object {
        val Native = OneInchToken(address = ContractAddress(value = "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"))
    }
}

val Token.asOneInchToken get() = when (this) {
    is Erc20Token -> OneInchToken(tokenAddress)
    is NativeToken -> OneInchToken.Native
}

fun OneInchToken.asToken(network: Network) = when (this) {
    OneInchToken.Native -> network.nativeCurrency
    else -> Erc20Token(network, address)
}
