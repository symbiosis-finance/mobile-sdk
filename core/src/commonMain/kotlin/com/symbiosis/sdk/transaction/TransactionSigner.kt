package com.symbiosis.sdk.transaction

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.gas.GasConfiguration
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.WalletAddress

/**
 * Blocking Transaction Signer exists to detect should a user show
 * any kind of progress for signing (is it remote or local)
 */
sealed interface TransactionSigner {
    suspend fun signTransferTransaction(
        nonce: BigInt,
        chainId: BigInt,
        to: WalletAddress,
        value: BigInt,
        gasConfiguration: GasConfiguration
    ): SignedTransaction
    suspend fun signContractTransaction(
        nonce: BigInt,
        chainId: BigInt,
        to: ContractAddress,
        contractData: String,
        value: BigInt,
        gasConfiguration: GasConfiguration
    ): SignedTransaction

    // Used for real crypto operations
    interface Blocking : TransactionSigner {
        override suspend fun signTransferTransaction(
            nonce: BigInt,
            chainId: BigInt,
            to: WalletAddress,
            value: BigInt,
            gasConfiguration: GasConfiguration
        ) = signTransferTransactionBlocking(nonce, chainId, to, value, gasConfiguration)

        override suspend fun signContractTransaction(
            nonce: BigInt,
            chainId: BigInt,
            to: ContractAddress,
            contractData: String,
            value: BigInt,
            gasConfiguration: GasConfiguration
        ) = signContractTransactionBlocking(nonce, chainId, to, contractData, value, gasConfiguration)

        fun signTransferTransactionBlocking(
            nonce: BigInt,
            chainId: BigInt,
            to: WalletAddress,
            value: BigInt,
            gasConfiguration: GasConfiguration
        ): SignedTransaction

        fun signContractTransactionBlocking(
            nonce: BigInt,
            chainId: BigInt,
            to: ContractAddress,
            contractData: String,
            value: BigInt,
            gasConfiguration: GasConfiguration
        ): SignedTransaction
    }
    // Used for custom services like MetaMask
    interface Async : TransactionSigner

}
