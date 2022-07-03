package com.symbiosis.sdk.swap.crosschain.executor

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.swap.crosschain.transaction.CrossChainSwapTransaction
import dev.icerock.moko.web3.signing.Credentials

interface CrossChainTradeExecutorAdapter {

    sealed interface ExecuteResult {
        class Sent(val transaction: CrossChainSwapTransaction) : ExecuteResult

        // if there is an exception while estimating
        object ExecutionRevertedWithoutSending : ExecuteResult
    }

    suspend fun execute(
        credentials: Credentials,
        deadline: BigInt? = null
    ): ExecuteResult
}
