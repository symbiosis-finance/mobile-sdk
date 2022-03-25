package com.symbiosis.sdk.internal.nonce

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.requests.Web3Requests
import dev.icerock.moko.web3.requests.executeBatch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NonceController(private val web3: Web3Executor) {
    @PublishedApi
    internal suspend fun getRemoteNonce(address: WalletAddress): BigInt =
        web3.executeBatch(Web3Requests.getNativeTransactionCount(address)).first()

    @PublishedApi
    internal val nonceCache = mutableMapOf<WalletAddress, BigInt>()

    @PublishedApi
    internal val mutex = Mutex()

    /**
     * All the requests should be sent sequentially, because we need possibility to rollback nonce
     */
    suspend inline fun <T> withNonce(
        address: WalletAddress,
        block: (nonce: BigInt) -> T
    ): T = mutex.withLock {
        val localNonce = nonceCache[address] ?: 0.bi
        val remoteNonce = getRemoteNonce(address)
        val nonce = if(localNonce > remoteNonce) localNonce else remoteNonce
        block(nonce).apply {
            nonceCache[address] = nonce
        }
    }
}
