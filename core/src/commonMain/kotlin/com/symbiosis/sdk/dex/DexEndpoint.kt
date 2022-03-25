package com.symbiosis.sdk.dex

import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bn
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.hex.Hex32String

/**
 * Decentralized Exchange Endpoint
 */
data class DexEndpoint(
    val factoryContractAddress: ContractAddress,
    val initCodeHash: Hex32String,
    val liquidityProviderFeePercent: BigNum
) {
    companion object {
        fun hardcoded(
            factoryContractAddress: String,
            initCodeHash: String,
            liquidityProviderFee: Double
        ) : DexEndpoint = DexEndpoint(
            factoryContractAddress = ContractAddress(value = factoryContractAddress),
            initCodeHash = Hex32String(initCodeHash),
            liquidityProviderFeePercent = liquidityProviderFee.bn
        )
    }
}
