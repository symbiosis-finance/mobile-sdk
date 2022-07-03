package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.internal.kbignum.UINT256_MAX
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.entity.TransactionHash
import dev.icerock.moko.web3.entity.WalletAddress
import dev.icerock.moko.web3.requests.executeBatch
import dev.icerock.moko.web3.requests.waitForTransactionReceipt
import dev.icerock.moko.web3.signing.Credentials

@Suppress("MemberVisibilityCanBePrivate")
class TokenContract internal constructor(
    private val network: Network,
    private val executor: Web3Executor,
    private val wrapped: SmartContract,
) {
    suspend fun name(): String = executor.executeBatch(nameRequest()).first()

    fun nameRequest() = wrapped.readRequest(
        method = "name",
        params = emptyList()
    ) { (name) -> name as String }

    suspend fun symbol(): String = executor.executeBatch(symbolRequest()).first()

    fun symbolRequest() = wrapped.readRequest(
        method = "symbol",
        params = emptyList()
    ) { (symbol) -> symbol as String }

    suspend fun decimals() = executor.executeBatch(decimalsRequest()).first()

    fun decimalsRequest() = wrapped.readRequest(
        method = "decimals",
        params = emptyList()
    ) { (decimals) -> decimals as BigInt }

    suspend fun approveMax(
        credentials: Credentials,
        spender: ContractAddress
    ): TransactionHash {
        val transferAmount = BigInt.UINT256_MAX

        val txHash = wrapped.write(
            credentials = credentials,
            method = "approve",
            params = listOf(
                spender,
                transferAmount
            )
        )

        executor.waitForTransactionReceipt(txHash)

        return txHash
    }

    fun balanceOfRequest(address: WalletAddress) =
        wrapped.readRequest(
            method = "balanceOf",
            params = listOf(address)
        ) { balances -> balances.firstOrNull() as BigInt? ?: 0.bi }

    suspend fun balanceOf(address: WalletAddress) =
        executor.executeBatch(balanceOfRequest(address)).first()

    fun allowanceRequest(owner: WalletAddress, spender: ContractAddress) =
        wrapped.readRequest(
            method = "allowance",
            params = listOf(owner, spender)
        ) { (allowance) -> allowance as BigInt }

    suspend fun allowance(owner: WalletAddress, spender: ContractAddress): BigInt =
        executor.executeBatch(allowanceRequest(owner, spender)).first()
}

suspend fun TokenContract.approveMaxIfNeed(
    credentials: Credentials,
    spender: ContractAddress,
    amount: BigInt
) {
    if (allowance(credentials.address, spender) < amount)
        approveMax(credentials, spender)
}

suspend fun TokenContract.checkTokenAllowance(
    address: WalletAddress,
    spender: ContractAddress,
    amount: BigInt
): Boolean = allowance(address, spender) >= amount
