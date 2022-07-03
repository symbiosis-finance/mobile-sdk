package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.swap.oneInch.OneInchToken
import dev.icerock.moko.web3.contract.SmartContract

class OracleContract(
    private val wrapped: SmartContract
) {
    fun getRateToEthRequest(srcToken: OneInchToken, useWrappers: Boolean) =
        wrapped
            .readRequest(
                method = "getRateToEth",
                params = listOf(srcToken.address, useWrappers),
                mapper = { (weightedRate) -> weightedRate as BigInt }
            )
}
