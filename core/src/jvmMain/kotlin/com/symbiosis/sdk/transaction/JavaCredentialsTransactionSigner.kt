package com.symbiosis.sdk.transaction

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.gas.GasConfiguration
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.WalletAddress
import java.math.BigInteger
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.utils.Numeric

class JavaCredentialsTransactionSigner(private val credentials: Credentials) : TransactionSigner.Blocking {

    override fun signTransferTransactionBlocking(
        nonce: BigInt,
        chainId: BigInt,
        to: WalletAddress,
        value: BigInt,
        gasConfiguration: GasConfiguration
    ): SignedTransaction {
        val transaction = when (gasConfiguration) {
            is GasConfiguration.Legacy -> RawTransaction.createEtherTransaction(
                BigInteger(nonce.toString()),
                BigInteger(gasConfiguration.gasPrice.toString()),
                BigInteger(gasConfiguration.gasLimit.toString()),
                to.withoutPrefix,
                BigInteger(value.toString())
            )
            is GasConfiguration.EIP1559 -> RawTransaction.createEtherTransaction(
                chainId.toString().toLong(),
                BigInteger(nonce.toString()),
                BigInteger(gasConfiguration.gasLimit.toString()),
                to.withoutPrefix,
                BigInteger(value.toString()),
                BigInteger(gasConfiguration.maxPriorityFeePerGas.toString()),
                BigInteger(gasConfiguration.maxFeePerGas.toString())
            )
        }
        val encoded = TransactionEncoder.signMessage(
            /* rawTransaction = */transaction,
            /* chainId = */chainId.toString().toLong(),
            /* credentials = */credentials
        )
        return SignedTransaction(Numeric.toHexString(encoded))
    }

    override fun signContractTransactionBlocking(
        nonce: BigInt,
        chainId: BigInt,
        to: ContractAddress,
        contractData: String,
        value: BigInt,
        gasConfiguration: GasConfiguration
    ): SignedTransaction {
        val transaction = when (gasConfiguration) {
            is GasConfiguration.Legacy -> RawTransaction.createTransaction(
                BigInteger(nonce.toString()),
                BigInteger(gasConfiguration.gasPrice.toString()),
                BigInteger(gasConfiguration.gasLimit.toString()),
                to.withoutPrefix,
                BigInteger(value.toString()),
                contractData
            )
            is GasConfiguration.EIP1559 -> RawTransaction.createTransaction(
                chainId.toString().toLong(),
                BigInteger(nonce.toString()),
                BigInteger(gasConfiguration.gasLimit.toString()),
                to.withoutPrefix,
                BigInteger(value.toString()),
                contractData,
                BigInteger(gasConfiguration.maxPriorityFeePerGas.toString()),
                BigInteger(gasConfiguration.maxFeePerGas.toString())
            )
        }
        val encoded = TransactionEncoder.signMessage(
            /* rawTransaction = */transaction,
            /* chainId = */chainId.toString().toLong(),
            /* credentials = */credentials
        )
        return SignedTransaction(Numeric.toHexString(encoded))
    }
}
