package com.symbiosis.sdk.contract

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.internal.nonce.NonceController
import com.symbiosis.sdk.network.sendTransaction
import com.symbiosis.sdk.transaction.SignedTransaction
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.requests.Web3Requests
import dev.icerock.moko.web3.requests.executeBatch

class WriteRequest(
    val contract: SmartContract,
    val method: String,
    val params: List<Any>,
    val value: BigInt,
) {
    val callData: HexString = contract.encodeMethod(method, params)

    suspend fun write(
        credentials: Credentials,
        chainId: BigInt,
        gasProvider: GasProvider,
        nonceController: NonceController
    ): TransactionHash =
        nonceController.withNonce(credentials.address) { nonce ->
            val signed: SignedTransaction = credentials.signer.signContractTransaction(
                nonce = nonce,
                chainId = chainId,
                value = value,
                gasConfiguration = gasProvider.getGasConfiguration(
                    from = credentials.address,
                    to = contract.contractAddress,
                    callData = callData,
                    value = value,
                    executor = contract.executor
                ),
                contractData = callData.prefixed,
                to = contract.contractAddress
            )
            return@withNonce contract.executor.sendTransaction(signed)
        }
}

fun SmartContract.writeRequest(
    method: String,
    params: List<Any>,
    value: BigInt = 0.bi,
) = WriteRequest(contract = this, method, params, value)

suspend fun SmartContract.write(
    chainId: BigInt,
    nonceController: NonceController,
    credentials: Credentials,
    method: String,
    params: List<Any>,
    gasProvider: GasProvider,
    value: BigInt = 0.bi,
) = writeRequest(method, params, value).write(credentials, chainId, gasProvider, nonceController)

fun Web3Requests.getEstimateGas(
    writeRequest: WriteRequest,
    from: EthereumAddress?,
    gasPrice: BigInt?,
) = getEstimateGas(
    from = from,
    gasPrice = gasPrice,
    to = writeRequest.contract.contractAddress,
    callData = writeRequest.callData,
    value = writeRequest.value
)

suspend fun Web3Executor.getEstimateGas(
    writeRequest: WriteRequest,
    from: EthereumAddress?,
    gasPrice: BigInt?
) = executeBatch(Web3Requests.getEstimateGas(writeRequest, from, gasPrice)).first()
