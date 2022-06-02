package com.symbiosis.sdk.swap.crosschain.bridging

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.swap.crosschain.CrossChainTokenPair
import com.symbiosis.sdk.swap.crosschain.SingleNetworkSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.StableSwapTradeAdapter
import dev.icerock.moko.web3.EthereumAddress

interface BridgingFeeProvider {
    suspend fun getBridgingFee(
        tokens: CrossChainTokenPair,
        inputTrade: SingleNetworkSwapTradeAdapter,
        stableTrade: StableSwapTradeAdapter,
        outputTrade: SingleNetworkSwapTradeAdapter,
        recipient: EthereumAddress
    ): BigInt
}
