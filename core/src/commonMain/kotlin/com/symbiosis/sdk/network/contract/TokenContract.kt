package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.contract.write
import com.symbiosis.sdk.internal.kbignum.UINT256_MAX
import com.symbiosis.sdk.internal.nonce.NonceController
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.requests.executeBatch

@Suppress("MemberVisibilityCanBePrivate")
class TokenContract internal constructor(
    private val network: Network,
    private val executor: Web3Executor,
    private val nonceController: NonceController,
    private val wrapped: SmartContract,
    private val defaultGasProvider: GasProvider
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
        spender: ContractAddress,
        gasProvider: GasProvider? = null
    ): TransactionHash {
        val transferAmount = BigInt.UINT256_MAX

        return wrapped.write(
            chainId = network.chainId,
            nonceController = nonceController,
            credentials = credentials,
            method = "approve",
            params = listOf(
                spender.bigInt,
                transferAmount
            ),
            gasProvider = gasProvider ?: defaultGasProvider
        )
    }

    fun balanceOfRequest(address: WalletAddress) =
        wrapped.readRequest(
            method = "balanceOf",
            params = listOf(address.bigInt)
        ) { balances -> balances.firstOrNull() as BigInt? ?: 0.bi }

    suspend fun balanceOf(address: WalletAddress) =
        executor.executeBatch(balanceOfRequest(address)).first()

    fun allowanceRequest(owner: WalletAddress, spender: ContractAddress) =
        wrapped.readRequest(
            method = "allowance",
            params = listOf(owner.bigInt, spender.bigInt)
        ) { (allowance) -> allowance as BigInt }

    suspend fun allowance(owner: WalletAddress, spender: ContractAddress): BigInt =
        executor.executeBatch(allowanceRequest(owner, spender)).first()
}

suspend fun TokenContract.approveMaxIfNeed(
    credentials: Credentials,
    spender: ContractAddress,
    amount: BigInt,
    gasProvider: GasProvider? = null
) {
    if (allowance(credentials.address, spender) < amount)
        approveMax(credentials, spender, gasProvider)
}

suspend fun TokenContract.checkTokenAllowance(
    address: WalletAddress,
    spender: ContractAddress,
    amount: BigInt
): Boolean = allowance(address, spender) >= amount
