package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.crosschain.executor.CrossChainTradeExecutorAdapter
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress

class CrossChainSwapRepository(private val adapter: Adapter) {

    sealed interface Result {
        // todo: add here stable token greater than max | stable token less than min
        class Success(val trade: CrossChainSwapTrade) : Result
        object TradeNotFound : Result
    }

    suspend fun findBestTradeExactIn(
        amountIn: BigInt,
        tokens: TokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress = from,
        bridgingFee: BigInt? = null
    ): Result {
        val pairAdapter = adapter.parsePair(tokens)

        val inputTrade = when (
            val result = adapter.inputTrade(
                amountIn = amountIn,
                firstTokenAddress = (tokens.first as? Erc20Token)?.tokenAddress,
                tokens = pairAdapter.inputPair,
                slippageTolerance = slippageTolerance,
            )
        ) {
            is Adapter.ExactInResult.Success -> result.trade
            is Adapter.ExactInResult.TradeNotFound -> return Result.TradeNotFound
        }

        val stableTrade = adapter.stableTrade(
            amountIn = inputTrade.amountOutMin,
            bridgingFee = bridgingFee ?: 0.bi,
        )

        val outputTrade = when (
            val result = adapter.outputTrade(
                amountIn = stableTrade.amountOutEstimated,
                bridgingFee = bridgingFee ?: 0.bi,
                firstTokenAddress = pairAdapter.stablePair.second.tokenAddress,
                tokens = pairAdapter.outputPair,
                slippageTolerance = slippageTolerance,
                recipient = recipient
            )
        ) {
            is Adapter.ExactInResult.Success -> result.trade
            is Adapter.ExactInResult.TradeNotFound -> return Result.TradeNotFound
        }

        if (bridgingFee == null) {
            val bridging = adapter.getBridgingFee(tokens, inputTrade, stableTrade, outputTrade, recipient)
            return findBestTradeExactIn(amountIn, tokens, slippageTolerance, from, recipient, bridging)
        }

        val crossChainTrade = CrossChainSwapTrade(
            amountIn = amountIn,
            amountOutEstimated = outputTrade.amountOutEstimated,
            inputTrade, stableTrade, outputTrade,
            bridgingFee = bridgingFee,
            slippageTolerance = slippageTolerance,
            recipient = recipient,
            executor = adapter.createExecutor(
                tokens,
                inputTrade,
                stableTrade,
                outputTrade,
                bridgingFee,
                from,
                recipient
            )
        )

        return Result.Success(crossChainTrade)
    }

    interface Adapter {
        fun parsePair(pair: TokenPair): TokenPairAdapter

        fun createExecutor(
            tokens: TokenPair,
            inputTrade: SingleNetworkSwapTradeAdapter,
            stableTrade: StableSwapTradeAdapter,
            outputTrade: SingleNetworkSwapTradeAdapter,
            bridgingFee: BigInt,
            fromAddress: EthereumAddress,
            recipient: EthereumAddress
        ): CrossChainTradeExecutorAdapter

        sealed interface ExactInResult {
            class Success(val trade: SingleNetworkSwapTradeAdapter) : ExactInResult
            object TradeNotFound : ExactInResult
        }

        suspend fun inputTrade(
            amountIn: BigInt,
            tokens: NetworkTokenPair,
            slippageTolerance: Percentage,
        ): ExactInResult

        suspend fun inputTrade(
            amountIn: BigInt,
            firstTokenAddress: ContractAddress?,
            tokens: NetworkTokenPair?,
            slippageTolerance: Percentage
        ): ExactInResult = when (tokens) {
            null -> ExactInResult.Success(
                SingleNetworkSwapTradeAdapter.Empty(amountIn, firstTokenAddress)
            )
            else -> inputTrade(amountIn, tokens, slippageTolerance)
        }

        suspend fun stableTrade(
            amountIn: BigInt,
            bridgingFee: BigInt,
        ): StableSwapTradeAdapter

        suspend fun outputTrade(
            amountIn: BigInt,
            bridgingFee: BigInt,
            tokens: NetworkTokenPair,
            slippageTolerance: Percentage,
            recipient: EthereumAddress
        ): ExactInResult

        suspend fun outputTrade(
            amountIn: BigInt,
            bridgingFee: BigInt,
            firstTokenAddress: ContractAddress?,
            tokens: NetworkTokenPair?,
            slippageTolerance: Percentage,
            recipient: EthereumAddress
        ): ExactInResult = when (tokens) {
            null -> ExactInResult.Success(
                SingleNetworkSwapTradeAdapter.Empty(amountIn, firstTokenAddress)
            )
            else -> outputTrade(amountIn, bridgingFee, tokens, slippageTolerance, recipient)
        }

        suspend fun getBridgingFee(
            tokens: TokenPair,
            inputTrade: SingleNetworkSwapTradeAdapter,
            stableTrade: StableSwapTradeAdapter,
            outputTrade: SingleNetworkSwapTradeAdapter,
            recipient: EthereumAddress
        ): BigInt
    }
}
