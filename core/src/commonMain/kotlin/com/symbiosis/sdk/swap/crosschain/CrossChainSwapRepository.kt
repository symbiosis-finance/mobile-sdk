package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.currency.amountRaw
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.crosschain.executor.CrossChainTradeExecutorAdapter
import dev.icerock.moko.web3.EthereumAddress

class CrossChainSwapRepository(private val adapter: Adapter) {

    sealed interface SwapResult {
        data class Success(val trade: CrossChainSwapTrade) : SwapResult
        object TradeNotFound : SwapResult

        class StableTokenLessThanBridgingFee(val actualInDollars: BigNum, val bridgingFee: TokenAmount) : SwapResult

        // Every trade has MAX | MIN a restriction: how many (in $) you can spend
        class StableTokenGreaterThanMax(val actualInDollars: BigNum, val maxInDollars: BigNum) : SwapResult
        class StableTokenLessThanMin(val actualInDollars: BigNum, val minInDollars: BigNum) : SwapResult
    }

    suspend fun findBestTradeExactIn(
        amountIn: BigInt,
        tokens: CrossChainTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress = from,
        bridgingFee: TokenAmount? = null
    ): SwapResult {
        val pairAdapter = adapter.parsePair(tokens)

        val inputTrade = when (
            val result = adapter.inputTrade(
                amountIn = amountIn,
                firstToken = tokens.first.asToken,
                tokens = pairAdapter.inputPair,
                slippageTolerance = slippageTolerance,
            )
        ) {
            is Adapter.ExactInResult.Success -> result.trade
            is Adapter.ExactInResult.TradeNotFound -> return SwapResult.TradeNotFound
        }

        val stableTrade = adapter.stableTrade(
            amountIn = inputTrade.amountOutMin,
            bridgingFee = bridgingFee?.raw ?: 0.bi,
        )

        val stableTokenInDollars = pairAdapter
            .stablePair
            .second
            .amountRaw(stableTrade.amountOutEstimated)
            .amount

        if (stableTokenInDollars < adapter.minStableTokenInDollars)
            return SwapResult.StableTokenLessThanMin(stableTokenInDollars, adapter.minStableTokenInDollars)

        if (stableTokenInDollars > adapter.maxStableTokenInDollars)
            return SwapResult.StableTokenGreaterThanMax(stableTokenInDollars, adapter.maxStableTokenInDollars)

        if (bridgingFee != null && stableTokenInDollars < bridgingFee.amount)
            return SwapResult.StableTokenLessThanBridgingFee(stableTokenInDollars, bridgingFee)

        val outputTrade = when (
            val result = adapter.outputTrade(
                amountIn = stableTrade.amountOutEstimated,
                bridgingFee = bridgingFee?.raw ?: 0.bi,
                firstToken = pairAdapter.stablePair.second.asToken,
                tokens = pairAdapter.outputPair,
                slippageTolerance = slippageTolerance,
                recipient = recipient
            )
        ) {
            is Adapter.ExactInResult.Success -> result.trade
            is Adapter.ExactInResult.TradeNotFound -> return SwapResult.TradeNotFound
        }

        if (bridgingFee == null) {
            val bridging = adapter.getBridgingFee(tokens, inputTrade, stableTrade, outputTrade, recipient)
            return findBestTradeExactIn(amountIn, tokens, slippageTolerance, from, recipient, bridging)
        }

        val crossChainTrade = CrossChainSwapTrade(
            amountIn = amountIn,
            amountOutEstimated = outputTrade.amountOutEstimated,
            amountOutMin = outputTrade.amountOutMin,
            inputTrade, stableTrade, outputTrade,
            bridgingFee = bridgingFee,
            slippageTolerance = slippageTolerance,
            recipient = recipient,
            executor = adapter.createExecutor(
                tokens,
                inputTrade,
                stableTrade,
                outputTrade,
                bridgingFee.raw,
                from,
                recipient
            )
        )

        return SwapResult.Success(crossChainTrade)
    }

    interface Adapter {
        val minStableTokenInDollars: BigNum
        val maxStableTokenInDollars: BigNum

        fun parsePair(pair: CrossChainTokenPair): TokenPairAdapter

        fun createExecutor(
            tokens: CrossChainTokenPair,
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
            tokens: SingleNetworkTokenPairAdapter,
            slippageTolerance: Percentage,
        ): ExactInResult

        suspend fun inputTrade(
            amountIn: BigInt,
            firstToken: Token,
            tokens: SingleNetworkTokenPairAdapter?,
            slippageTolerance: Percentage
        ): ExactInResult = when (tokens) {
            null -> ExactInResult.Success(
                SingleNetworkSwapTradeAdapter.Empty(amountIn, firstToken)
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
            tokens: SingleNetworkTokenPairAdapter,
            slippageTolerance: Percentage,
            recipient: EthereumAddress
        ): ExactInResult

        suspend fun outputTrade(
            amountIn: BigInt,
            bridgingFee: BigInt,
            firstToken: Token,
            tokens: SingleNetworkTokenPairAdapter?,
            slippageTolerance: Percentage,
            recipient: EthereumAddress
        ): ExactInResult = when (tokens) {
            null -> ExactInResult.Success(
                SingleNetworkSwapTradeAdapter.Empty(amountIn, firstToken)
            )
            else -> outputTrade(amountIn, bridgingFee, tokens, slippageTolerance, recipient)
        }

        suspend fun getBridgingFee(
            tokens: CrossChainTokenPair,
            inputTrade: SingleNetworkSwapTradeAdapter,
            stableTrade: StableSwapTradeAdapter,
            outputTrade: SingleNetworkSwapTradeAdapter,
            recipient: EthereumAddress
        ): TokenAmount
    }
}
