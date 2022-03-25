package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.contract.write
import com.symbiosis.sdk.crosschain.CrossChainClient
import com.symbiosis.sdk.internal.nonce.NonceController
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.swap.meta.NerveStablePool
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.requests.executeBatch

class NerveContract(
    private val wrapped: SmartContract,
    private val network: Network,
    private val nonceController: NonceController,
    private val executor: Web3Executor,
    private val defaultGasProvider: GasProvider,
) {
    fun calculateSwapRequest(
        tokenIndexFrom: Int,
        tokenIndexTo: Int,
        amount: BigInt
    ) = wrapped.readRequest(
        method = "calculateSwap",
        params = listOf(
            tokenIndexFrom.bi,
            tokenIndexTo.bi,
            amount
        )
    ) { result ->
        if (result.isEmpty())
            return@readRequest 0.bi

        return@readRequest result.first() as BigInt
    }

    suspend fun calculateSwap(
        tokenIndexFrom: Int,
        tokenIndexTo: Int,
        amount: BigInt
    ) = executor.executeBatch(
        calculateSwapRequest(
            tokenIndexFrom = tokenIndexFrom,
            tokenIndexTo = tokenIndexTo,
            amount = amount
        )
    ).first()

    suspend fun swap(
        credentials: Credentials,
        tokenIndexFrom: BigInt,
        tokenIndexTo: BigInt,
        dx: BigInt,
        minDy: BigInt,
        deadline: BigInt,
        value: BigInt,
        gasProvider: GasProvider? = null
    ) = wrapped.write(
        chainId = network.chainId,
        nonceController = nonceController,
        credentials = credentials,
        method = "swap",
        params = listOf(
            tokenIndexFrom,
            tokenIndexTo,
            dx,
            minDy,
            deadline
        ),
        gasProvider = gasProvider ?: defaultGasProvider,
        value = value
    )

    fun getSwapCallData(
        tokenIndexFrom: BigInt,
        tokenIndexTo: BigInt,
        dx: BigInt,
        minDy: BigInt,
        deadline: BigInt,
    ) = wrapped.encodeMethod(
        method = "swap",
        params = listOf(
            tokenIndexFrom,
            tokenIndexTo,
            dx,
            minDy,
            deadline
        )
    )


    /**
     * Method that provides token index on stable pools according to metaRouter direction
     * @see CrossChainClient.findBestTradeExactIn
     */
    fun getTokenIndex(
        nerveStablePool: NerveStablePool,
        tokenAddress: ContractAddress
    ): BigInt = nerveStablePool.tokens.indexOfFirst { it.tokenAddress == tokenAddress }.bi
}