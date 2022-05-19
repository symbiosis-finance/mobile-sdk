package com.symbiosis.sdk.swap

interface PendingTransaction {
    suspend fun wait()
}
