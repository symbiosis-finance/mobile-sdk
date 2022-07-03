package com.symbiosis.sdk.swap.oneInch

import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.DecimalsNativeToken
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.entity.ContractAddress

class OneInchToken(val address: ContractAddress, val decimals: Int) {
    companion object {
        val Native = OneInchToken(
            address = ContractAddress(value = "0xEeeeeEeeeEeEeeEeEeEeeEEEeeeeEeeeeeeeEEeE"),
            decimals = 18
        )
    }
}

val DecimalsToken.asOneInchToken get() = when (this) {
    is DecimalsErc20Token -> OneInchToken(tokenAddress, decimals)
    is DecimalsNativeToken -> OneInchToken.Native
}

fun OneInchToken.asToken(network: Network): DecimalsToken = when (this) {
    OneInchToken.Native -> network.nativeCurrency
    else -> DecimalsErc20Token(network, address, decimals)
}
