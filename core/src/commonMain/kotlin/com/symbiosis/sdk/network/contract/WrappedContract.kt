package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.contract.write
import com.symbiosis.sdk.internal.nonce.NonceController
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.SmartContract

class WrappedContract internal constructor(
    private val executor: Web3Executor,
    private val wrapped: SmartContract,
    private val network: Network,
    private val nonceController: NonceController,
    private val defaultGasProvider: GasProvider
) {
    suspend fun wrap(
        amount: BigInt,
        credentials: Credentials,
        gasProvider: GasProvider? = null
    ) = wrapped.write(
        chainId = network.chainId,
        nonceController = nonceController,
        credentials = credentials,
        method = "deposit",
        params = listOf(),
        value = amount,
        gasProvider = gasProvider ?: defaultGasProvider,
    )

    suspend fun unwrap(
        amount: BigInt,
        credentials: Credentials,
        gasProvider: GasProvider? = null
    ) = wrapped.write(
        chainId = network.chainId,
        nonceController = nonceController,
        credentials = credentials,
        method = "withdraw",
        params = listOf(amount),
        gasProvider = gasProvider ?: defaultGasProvider
    )
}
