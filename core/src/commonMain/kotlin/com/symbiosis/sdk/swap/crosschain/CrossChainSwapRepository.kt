package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.currency.TokenPair
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
        amountIn: TokenAmount,
        tokens: TokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress = from,
        bridgingFee: TokenAmount? = null
    ): SwapResult {
        require(amountIn.token == tokens.first)

        val pairAdapter = adapter.parsePair(tokens)

        val inputTrade = when (
            val result = adapter.inputTradeNullable(
                amountIn = amountIn,
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
            slippageTolerance = slippageTolerance,
        )

        val stableTokenInDollars = stableTrade.amountOutEstimated.amount

        if (stableTokenInDollars < adapter.minStableTokenInDollars)
            return SwapResult.StableTokenLessThanMin(stableTokenInDollars, adapter.minStableTokenInDollars)

        if (stableTokenInDollars > adapter.maxStableTokenInDollars)
            return SwapResult.StableTokenGreaterThanMax(stableTokenInDollars, adapter.maxStableTokenInDollars)

        if (bridgingFee != null && stableTokenInDollars < bridgingFee.amount)
            return SwapResult.StableTokenLessThanBridgingFee(stableTokenInDollars, bridgingFee)

        val outputTrade = when (
            val result = adapter.outputTradeNullable(
                amountIn = stableTrade.amountOutEstimated,
                bridgingFee = bridgingFee?.raw ?: 0.bi,
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

        val dollarsAmount = adapter.dollarsAmount(stableTrade.amountOutEstimated, bridgingFee)

        val crossChainTrade = CrossChainSwapTrade(
            tokens = tokens,
            amountIn = amountIn,
            amountOutEstimated = outputTrade.amountOutEstimated,
            amountOutMin = outputTrade.amountOutMin,
            dollarsAmount = dollarsAmount,
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
                recipient,
                amountIn
            )
        )

        return SwapResult.Success(crossChainTrade)
    }

    interface Adapter {
        val minStableTokenInDollars: BigNum
        val maxStableTokenInDollars: BigNum

        fun parsePair(pair: TokenPair): TokenPairAdapter

        fun createExecutor(
            tokens: TokenPair,
            inputTrade: SingleNetworkSwapTradeAdapter,
            stableTrade: StableSwapTradeAdapter,
            outputTrade: SingleNetworkSwapTradeAdapter,
            bridgingFee: BigInt,
            fromAddress: EthereumAddress,
            recipient: EthereumAddress,
            amountIn: TokenAmount
        ): CrossChainTradeExecutorAdapter

        sealed interface ExactInResult {
            class Success(val trade: SingleNetworkSwapTradeAdapter) : ExactInResult
            object TradeNotFound : ExactInResult
        }

        suspend fun inputTrade(
            amountIn: TokenAmount,
            tokens: NetworkTokenPair,
            slippageTolerance: Percentage,
        ): ExactInResult

        suspend fun inputTradeNullable(
            amountIn: TokenAmount,
            tokens: NetworkTokenPair?,
            slippageTolerance: Percentage
        ): ExactInResult = when (tokens) {
            null -> ExactInResult.Success(
                SingleNetworkSwapTradeAdapter.Empty(amountIn)
            )
            else -> inputTrade(amountIn, tokens, slippageTolerance)
        }

        suspend fun stableTrade(
            amountIn: TokenAmount,
            bridgingFee: BigInt,
            slippageTolerance: Percentage
        ): StableSwapTradeAdapter

        suspend fun outputTrade(
            amountIn: TokenAmount,
            bridgingFee: BigInt,
            tokens: NetworkTokenPair,
            slippageTolerance: Percentage,
            recipient: EthereumAddress
        ): ExactInResult

        suspend fun outputTradeNullable(
            amountIn: TokenAmount,
            bridgingFee: BigInt,
            tokens: NetworkTokenPair?,
            slippageTolerance: Percentage,
            recipient: EthereumAddress
        ): ExactInResult = when (tokens) {
            null -> ExactInResult.Success(
                SingleNetworkSwapTradeAdapter.Empty(amountIn)
            )
            else -> outputTrade(amountIn, bridgingFee, tokens, slippageTolerance, recipient)
        }

        suspend fun getBridgingFee(
            tokens: TokenPair,
            inputTrade: SingleNetworkSwapTradeAdapter,
            stableTrade: StableSwapTradeAdapter,
            outputTrade: SingleNetworkSwapTradeAdapter,
            recipient: EthereumAddress
        ): TokenAmount

        fun dollarsAmount(stableTradeAmountOut: TokenAmount, bridgingFee: TokenAmount): BigNum
    }
}
