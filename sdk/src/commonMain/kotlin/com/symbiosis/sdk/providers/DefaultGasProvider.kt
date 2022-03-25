package com.symbiosis.sdk.providers

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.gas.GasConfiguration
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.requests.getEstimateGas
import dev.icerock.moko.web3.requests.getGasPrice

object DefaultGasProvider : GasProvider {
    private suspend fun gasPrice(executor: Web3Executor): BigInt = executor.getGasPrice()
    private suspend fun gasLimit(
        executor: Web3Executor,
        from: EthereumAddress?,
        to: ContractAddress,
        gasPrice: BigInt?,
        callData: HexString?,
        value: BigInt?
    ): BigInt = executor.getEstimateGas(
        from = from,
        gasPrice = gasPrice(executor),
        to = to,
        callData = callData,
        value = value
    )

    override suspend fun getGasConfiguration(
        from: EthereumAddress?,
        to: ContractAddress,
        callData: HexString?,
        value: BigInt?,
        executor: Web3Executor
    ): GasConfiguration =
        GasConfiguration.Legacy(
            gasPrice = gasPrice(executor),
            gasLimit = gasLimit(
                from = from,
                to = to,
                gasPrice = gasPrice(executor),
                callData = callData,
                value = value,
                executor = executor
            )
        )
}