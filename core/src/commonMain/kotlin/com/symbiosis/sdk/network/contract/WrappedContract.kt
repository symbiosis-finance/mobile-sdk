package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.signing.Credentials

class WrappedContract internal constructor(
    private val executor: Web3Executor,
    private val wrapped: SmartContract,
    private val network: Network
) {
    suspend fun wrap(
        amount: BigInt,
        credentials: Credentials
    ) = wrapped.write(
        credentials = credentials,
        method = "deposit",
        params = listOf(),
        value = amount
    )

    suspend fun unwrap(
        amount: BigInt,
        credentials: Credentials
    ) = wrapped.write(
        credentials = credentials,
        method = "withdraw",
        params = listOf(amount)
    )
}
