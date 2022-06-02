package com.symbiosis.sdk.swap.crosschain.bridging

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.swap.crosschain.CrossChain
import com.symbiosis.sdk.swap.crosschain.CrossChainTokenPair
import com.symbiosis.sdk.swap.crosschain.SingleNetworkSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.StableSwapTradeAdapter
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.hex.HexString

class DefaultBridgingFeeProvider(private val adapterFactory: Adapter.Factory) : BridgingFeeProvider {
    private val api = SymbiosisBridgingApi

    override suspend fun getBridgingFee(
        tokens: CrossChainTokenPair,
        inputTrade: SingleNetworkSwapTradeAdapter,
        stableTrade: StableSwapTradeAdapter,
        outputTrade: SingleNetworkSwapTradeAdapter,
        recipient: EthereumAddress
    ): BigInt {
        val crossChain = when (stableTrade) {
            is StableSwapTradeAdapter.Default -> stableTrade.underlying.crossChain
        }

        val adapter = adapterFactory.create(crossChain)

        return api.getBridgingFee(
            chainFromId = crossChain.fromNetwork.chainId,
            chainToId = crossChain.toNetwork.chainId,
            receiveSide = adapter.receiveSide,
            callData = adapter.callData(
                tokens,
                inputTrade,
                stableTrade,
                outputTrade,
                recipient
            )
        )
    }

    interface Adapter {
        val receiveSide: ContractAddress

        suspend fun callData(
            tokens: CrossChainTokenPair,
            inputTrade: SingleNetworkSwapTradeAdapter,
            stableTrade: StableSwapTradeAdapter,
            outputTrade: SingleNetworkSwapTradeAdapter,
            recipient: EthereumAddress
        ): HexString

        interface Factory {
            fun create(crossChain: CrossChain): Adapter
        }
    }
}