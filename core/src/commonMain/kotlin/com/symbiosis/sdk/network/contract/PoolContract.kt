package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.swap.Reserves
import com.symbiosis.sdk.swap.ReservesData
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.requests.executeBatch

class PoolContract internal constructor(
    private val isReversed: Boolean,
    private val executor: Web3Executor,
    private val wrapped: SmartContract,
) {
    val address = wrapped.contractAddress

    fun getReservesRequest() = wrapped.readRequest(
        method = "getReserves",
        params = listOf()
    ) { reserves ->
        if (reserves.isEmpty())
            return@readRequest Reserves.Empty

        val (reserve1, reserve2) = reserves
        when (isReversed) {
            true -> ReservesData(reserve2 as BigInt, reserve1 as BigInt)
            false -> ReservesData(reserve1 as BigInt, reserve2 as BigInt)
        }
    }

    suspend fun getReserves() = executor.executeBatch(getReservesRequest()).first()
}
