package com.symbiosis.sdk.swap.crosschain.bridging

import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.swap.crosschain.CrossChain
import com.symbiosis.sdk.swap.crosschain.SingleNetworkSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.StableSwapTradeAdapter
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.hex.HexString

class DefaultBridgingFeeProvider(private val adapterFactory: Adapter.Factory) : BridgingFeeProvider {
    private val api = SymbiosisBridgingApi

    override suspend fun getBridgingFee(
        tokens: TokenPair,
        inputTrade: SingleNetworkSwapTradeAdapter,
        stableTrade: StableSwapTradeAdapter,
        outputTrade: SingleNetworkSwapTradeAdapter,
        recipient: EthereumAddress
    ): TokenAmount {
        val crossChain = when (stableTrade) {
            is StableSwapTradeAdapter.Default -> stableTrade.underlying.crossChain
        }

        val feeToken = when (crossChain.hasPoolOnFirstNetwork) {
            true -> stableTrade.synthToken
            false -> stableTrade.tokens.first
        }

        val adapter = adapterFactory.create(crossChain)

        return api.getBridgingFee(
            advisorUrl = crossChain.advisorUrl,
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
        ).let { bridgingFee -> TokenAmount(bridgingFee, feeToken) }
    }

    interface Adapter {
        val receiveSide: ContractAddress

        suspend fun callData(
            tokens: TokenPair,
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