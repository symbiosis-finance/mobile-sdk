package com.symbiosis.sdk.swap.unified

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.crosschain.CrossChain
import com.symbiosis.sdk.swap.crosschain.CrossChainSwapRepository
import com.symbiosis.sdk.swap.crosschain.SymbiosisCrossChainClient
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkSwapRepository
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTokenPair
import com.symbiosis.sdk.swap.unified.UnifiedSwapRepository.Adapter.CrossChainSwapResult
import com.symbiosis.sdk.swap.unified.UnifiedSwapRepository.Adapter.SingleNetworkSwapResult
import com.symbiosis.sdk.symbiosisClient
import dev.icerock.moko.web3.entity.EthereumAddress

class DefaultUnifiedSwapRepositoryAdapter(
    private val crossChains: List<CrossChain>
) : UnifiedSwapRepository.Adapter {
    override suspend fun findSingleNetworkTrade(
        amountIn: TokenAmount,
        tokens: NetworkTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress
    ): SingleNetworkSwapResult {
        val networkClient = tokens.network.symbiosisClient

        val tokensAdapter = SingleNetworkTokenPair(tokens.first, tokens.second)

        val swap = when (
            val result = networkClient.swap.exactIn(
                amountIn, tokensAdapter,
                slippageTolerance,
                from, recipient
            )
        ) {
            is SingleNetworkSwapRepository.ExactInResult.Success ->
                result.trade
            is SingleNetworkSwapRepository.ExactInResult.TradeNotFound ->
                return SingleNetworkSwapResult.TradeNotFound
        }

        return SingleNetworkSwapResult
            .Success(UnifiedSwapTrade.SingleNetwork.Default(swap))
    }

    override suspend fun findCrossChainTrade(
        amountIn: TokenAmount,
        tokens: TokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress
    ): CrossChainSwapResult {
        val crossChainClient = findCrossChain(tokens.first.network.chainId, tokens.second.network.chainId)!!
            .let(::SymbiosisCrossChainClient)

        val swap = when (
            val result = crossChainClient.findBestTradeExactIn(
                amountIn, tokens,
                from, recipient,
                slippageTolerance
            )
        ) {
            is CrossChainSwapRepository.SwapResult.StableTokenGreaterThanMax ->
                return CrossChainSwapResult.StableTokensGreaterThanMax(result.actualInDollars, result.maxInDollars)
            is CrossChainSwapRepository.SwapResult.StableTokenLessThanBridgingFee ->
                return CrossChainSwapResult.StableTokensLessThanBridgingFee(result.actualInDollars, result.bridgingFee)
            is CrossChainSwapRepository.SwapResult.StableTokenLessThanMin ->
                return CrossChainSwapResult.StableTokensLessThanMin(result.actualInDollars, result.minInDollars)
            is CrossChainSwapRepository.SwapResult.TradeNotFound ->
                return CrossChainSwapResult.TradeNotFound
            is CrossChainSwapRepository.SwapResult.Success ->
                result.trade
        }

        return CrossChainSwapResult.Success(UnifiedSwapTrade.CrossChain.Default(swap))
    }

    private fun findCrossChain(inputNetworkChainId: BigInt, outputNetworkChainId: BigInt): CrossChain? =
        crossChains.firstOrNull {
            it.fromNetwork.chainId == inputNetworkChainId &&
                    it.toNetwork.chainId == outputNetworkChainId
        }

    override suspend fun getAllowedRangeForInput(
        inputToken: DecimalsToken,
        outputNetwork: Network,
        slippageTolerance: Percentage
    ): UnifiedSwapRepository.AllowedRangeResult {
        val crossChainClient = findCrossChain(inputToken.network.chainId, outputNetwork.chainId)!!
            .let(::SymbiosisCrossChainClient)

        return when (val result = crossChainClient.getAllowedRangeForInput(inputToken, slippageTolerance)) {
            is SymbiosisCrossChainClient.AllowedRangeResult.Success ->
                UnifiedSwapRepository.AllowedRangeResult.Limited(result.minAmount, result.maxAmount)
            is SymbiosisCrossChainClient.AllowedRangeResult.TradeNotFound ->
                UnifiedSwapRepository.AllowedRangeResult.NoLimit
        }
    }

    override fun isCrossChainSupported(inputNetworkChainId: BigInt, outputNetworkChainId: BigInt): Boolean =
        findCrossChain(inputNetworkChainId, outputNetworkChainId) != null
}
