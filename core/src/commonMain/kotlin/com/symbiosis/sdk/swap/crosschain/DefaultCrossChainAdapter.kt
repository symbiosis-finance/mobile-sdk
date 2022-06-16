package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.currency.amountRaw
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

    override val minStableTokenInDollars: BigNum = crossChain.minStableTokensAmountPerTrade
    override val maxStableTokenInDollars: BigNum = crossChain.maxStableTokensAmountPerTrade

    override fun parsePair(pair: TokenPair): TokenPairAdapter {
        require(pair.first.asToken.network.chainId == crossChain.fromNetwork.chainId) {
            "Invalid crossChain selected"
        }
        require(pair.second.asToken.network.chainId == crossChain.toNetwork.chainId) {
            "Invalid crossChain selected"
        }

        return DefaultTokenPairAdapter(pair, crossChain)
    }

    override fun createExecutor(
        tokens: TokenPair,
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
        amountIn: TokenAmount,
        tokens: NetworkTokenPair,
        slippageTolerance: Percentage
    ): ExactInResult = inputSingleNetworkSwap.exactIn(
        amountIn = amountIn,
        tokens = tokens,
        slippageTolerance = slippageTolerance,
        from = inputNetwork.metaRouterAddress,
        recipient = inputNetwork.metaRouterAddress
    )

    override suspend fun stableTrade(amountIn: TokenAmount, bridgingFee: BigInt): StableSwapTradeAdapter {
        val amountInInt = when (crossChain.hasPoolOnSecondNetwork) {
            true -> (amountIn.raw - bridgingFee).let { int -> if (int > 0.bi) int else 0.bi }
            false -> amountIn.raw
        }

        return stable.findBestTrade(
            amountIn = amountIn.token.amountRaw(amountInInt)
        )
    }

    override suspend fun outputTrade(
        amountIn: TokenAmount,
        bridgingFee: BigInt,
        tokens: NetworkTokenPair,
        slippageTolerance: Percentage,
        recipient: EthereumAddress
    ): ExactInResult {
        val amountInInt = when (crossChain.hasPoolOnFirstNetwork) {
            true -> (amountIn.raw - bridgingFee).let { int -> if (int > 0.bi) int else 0.bi }
            false -> amountIn.raw
        }

        return outputSingleNetworkSwap.exactIn(
            amountIn = amountIn.token.amountRaw(amountInInt),
            tokens = tokens,
            slippageTolerance = slippageTolerance,
            from = outputNetwork.metaRouterAddress,
            recipient = recipient
        )
    }

    override suspend fun getBridgingFee(
        tokens: TokenPair,
        inputTrade: SingleNetworkSwapTradeAdapter,
        stableTrade: StableSwapTradeAdapter,
        outputTrade: SingleNetworkSwapTradeAdapter,
        recipient: EthereumAddress
    ): TokenAmount = bridgingFeeProvider.getBridgingFee(tokens, inputTrade, stableTrade, outputTrade, recipient)

    override fun dollarsAmount(stableTradeAmountOut: TokenAmount, bridgingFee: TokenAmount): BigNum =
        when (crossChain.hasPoolOnFirstNetwork) {
            true -> stableTradeAmountOut.amount
            false -> stableTradeAmountOut.amount + bridgingFee.amount
        }
}
