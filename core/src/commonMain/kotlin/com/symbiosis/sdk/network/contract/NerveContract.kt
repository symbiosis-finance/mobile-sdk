package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import com.symbiosis.sdk.swap.crosschain.SymbiosisCrossChainClient
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.requests.executeBatch
import dev.icerock.moko.web3.signing.Credentials

class NerveContract(
    private val wrapped: SmartContract,
    private val executor: Web3Executor
) {
    fun calculateSwapRequest(
        tokenIndexFrom: BigInt,
        tokenIndexTo: BigInt,
        amount: BigInt
    ) = wrapped.readRequest(
        method = "calculateSwap",
        params = listOf(
            tokenIndexFrom,
            tokenIndexTo,
            amount
        )
    ) { result ->
        if (result.isEmpty())
            return@readRequest 0.bi

        return@readRequest result.first() as BigInt
    }

    suspend fun calculateSwap(
        tokenIndexFrom: BigInt,
        tokenIndexTo: BigInt,
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
        deadline: BigInt
    ) = wrapped.write(
        credentials = credentials,
        method = "swap",
        params = listOf(
            tokenIndexFrom,
            tokenIndexTo,
            dx,
            minDy,
            deadline
        )
    )

    fun getSwapCallData(
        tokenIndexFrom: BigInt,
        tokenIndexTo: BigInt,
        dx: BigInt,
        minDy: BigInt,
        deadline: BigInt,
    ) = wrapped.writeRequest(
        method = "swap",
        params = listOf(
            tokenIndexFrom,
            tokenIndexTo,
            dx,
            minDy,
            deadline
        )
    ).callData


    /**
     * Method that provides token index on stable pools according to metaRouter direction
     * @see SymbiosisCrossChainClient.findBestTradeExactIn
     */
    fun getTokenIndex(
        nerveStablePool: NerveStablePool,
        tokenAddress: ContractAddress
    ): BigInt = nerveStablePool.tokens.indexOfFirst { it.tokenAddress == tokenAddress }.bi
}