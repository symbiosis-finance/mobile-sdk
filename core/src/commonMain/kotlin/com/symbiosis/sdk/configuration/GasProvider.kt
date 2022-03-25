package com.symbiosis.sdk.configuration

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.gas.GasConfiguration
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.hex.HexString

/**
 * This one allows to set a custom default gas fee per transaction
 */
fun interface GasProvider {
    suspend fun getGasConfiguration(
        from: EthereumAddress?,
        to: ContractAddress,
        callData: HexString?,
        value: BigInt?,
        executor: Web3Executor
    ): GasConfiguration
}
