package com.symbiosis.sdk.swap.crosschain.executor

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.wallet.Credentials

interface CrossChainTradeExecutorAdapter {
    suspend fun execute(credentials: Credentials, deadline: BigInt? = null, gasProvider: GasProvider? = null)
}
