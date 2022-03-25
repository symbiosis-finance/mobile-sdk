package com.symbiosis.sdk.network

import com.symbiosis.sdk.transaction.SignedTransaction
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.requests.send


suspend fun Web3Executor.sendTransaction(signedTransaction: SignedTransaction) =
    send(signedTransaction.value)
