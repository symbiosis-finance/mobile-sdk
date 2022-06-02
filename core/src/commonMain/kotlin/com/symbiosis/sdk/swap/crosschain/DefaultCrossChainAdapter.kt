package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.crosschain.CrossChainSwapRepository.Adapter.ExactInResult
import com.symbiosis.sdk.swap.crosschain.bridging.BridgingFeeProvider
import com.symbiosis.sdk.swap.crosschain.executor.CrossChainTradeExecutorAdapter
import dev.icerock.moko.web3.EthereumAddress

class DefaultCrossChainAdapter(
    private val crossChain: CrossChain,
    private val inputSingleNetworkSwap: SingleNetworkSwapRepositoryAdapter,
    private val stable: StableSwapRepositoryAdapter,
    private val outputSingleNetworkSwap: SingleNetworkSwapRepositoryAdapter,
    private val bridgingFeeProvider: BridgingFeeProvider
) : CrossChainSwapRepository.Adapter {
    private val inputNetwork = crossChain.fromNetwork
    private val outputNetwork = crossChain.toNetwork

    override fun parsePair(pair: CrossChainTokenPair): TokenPairAdapter {
        require(pair.first.asToken.network.chainId == crossChain.fromNetwork.chainId) {
            "Invalid crossChain selected"
        }
        require(pair.second.asToken.network.chainId == crossChain.toNetwork.chainId) {
            "Invalid crossChain selected"
        }

        return DefaultTokenPairAdapter(pair, crossChain)
    }

    override fun createExecutor(
        tokens: CrossChainTokenPair,
        inputTrade: SingleNetworkSwapTradeAdapter,
        stableTrade: StableSwapTradeAdapter,
        outputTrade: SingleNetworkSwapTradeAdapter,
        bridgingFee: BigInt,
        fromAddress: EthereumAddress,
        recipient: EthereumAddress
    ): CrossChainTradeExecutorAdapter = CrossChainTradeExecutorAdapter(
        crossChain, inputTrade, stableTrade, outputTrade,
        tokens, bridgingFee, fromAddress, recipient
    )

    override suspend fun inputTrade(
        amountIn: BigInt,
        tokens: SingleNetworkTokenPairAdapter,
        slippageTolerance: Percentage
    ): ExactInResult = inputSingleNetworkSwap.exactIn(
        amountIn = amountIn,
        tokens = tokens,
        slippageTolerance = slippageTolerance,
        from = inputNetwork.metaRouterAddress,
        recipient = inputNetwork.metaRouterAddress
    )

    override suspend fun stableTrade(amountIn: BigInt, bridgingFee: BigInt): StableSwapTradeAdapter {
        return stable.findBestTrade(
            amountIn = when (crossChain.hasPoolOnSecondNetwork) {
                true -> (amountIn - bridgingFee).let { int -> if (int > 0.bi) int else 0.bi }
                false -> amountIn
            }
        )
    }

    override suspend fun outputTrade(
        amountIn: BigInt,
        bridgingFee: BigInt,
        tokens: SingleNetworkTokenPairAdapter,
        slippageTolerance: Percentage,
        recipient: EthereumAddress
    ): ExactInResult {
        return outputSingleNetworkSwap.exactIn(
            amountIn = when (crossChain.hasPoolOnFirstNetwork) {
                true -> (amountIn - bridgingFee).let { int -> if (int > 0.bi) int else 0.bi }
                false -> amountIn
            },
            tokens = tokens,
            slippageTolerance = slippageTolerance,
            from = outputNetwork.metaRouterAddress,
            recipient = recipient
        )
    }

    override suspend fun getBridgingFee(
        tokens: CrossChainTokenPair,
        inputTrade: SingleNetworkSwapTradeAdapter,
        stableTrade: StableSwapTradeAdapter,
        outputTrade: SingleNetworkSwapTradeAdapter,
        recipient: EthereumAddress
    ): BigInt = bridgingFeeProvider.getBridgingFee(tokens, inputTrade, stableTrade, outputTrade, recipient)
}
