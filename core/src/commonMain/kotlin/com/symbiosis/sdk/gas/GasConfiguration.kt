package com.symbiosis.sdk.gas

import com.soywiz.kbignum.BigInt

sealed interface GasConfiguration {
    class Legacy(
        val gasPrice: BigInt,
        val gasLimit: BigInt
    ) : GasConfiguration

    class EIP1559(
        val gasLimit: BigInt,
        val maxPriorityFeePerGas: BigInt,
        val maxFeePerGas: BigInt
    ) : GasConfiguration
}
