package com.symbiosis.sdk.swap.unified

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.swap.Percentage
import dev.icerock.moko.web3.entity.EthereumAddress

class UnifiedSwapRepository(private val adapter: Adapter) {

    sealed interface SwapResult {
        data class Success(val trade: UnifiedSwapTrade) : SwapResult
        data class StableTokensLessThanBridgingFee(val actualInDollars: BigNum, val bridgingFee: TokenAmount) : SwapResult
        data class StableTokensLessThanMin(val actualInDollars: BigNum, val minInDollars: BigNum) : SwapResult
        data class StableTokensGreaterThanMax(val actualInDollars: BigNum, val maxInDollars: BigNum) : SwapResult
        object TradeNotFound : SwapResult
    }

    suspend fun findBestTrade(
        amountIn: BigInt,
        tokens: TokenPair,
        from: EthereumAddress,
        recipient: EthereumAddress = from,
        slippageTolerance: Percentage = Percentage(0.07.bn)
    ) = findBestTrade(
        TokenAmount(amountIn, tokens.first),
        tokens, from, recipient, slippageTolerance
    )

    suspend fun findBestTrade(
        amountIn: TokenAmount,
        tokens: TokenPair,
        from: EthereumAddress,
        recipient: EthereumAddress = from,
        slippageTolerance: Percentage = Percentage(0.07.bn)
    ): SwapResult {
        requireChainsSupported(tokens.first.network, tokens.second.network)

        val networkPair = tokens.asNetworkTokenPair()

        if (networkPair != null)
            return when (
                val result = adapter.findSingleNetworkTrade(
                    amountIn, networkPair, slippageTolerance, from, recipient
                )
            ) {
                is Adapter.SingleNetworkSwapResult.Success ->
                    SwapResult.Success(result.trade)
                is Adapter.SingleNetworkSwapResult.TradeNotFound ->
                    SwapResult.TradeNotFound
            }

        return when (
            val result = adapter.findCrossChainTrade(
                amountIn, tokens, slippageTolerance, from, recipient
            )
        ) {
            is Adapter.CrossChainSwapResult.StableTokensGreaterThanMax ->
                SwapResult.StableTokensGreaterThanMax(result.actualInDollars, result.maxInDollars)
            is Adapter.CrossChainSwapResult.StableTokensLessThanBridgingFee ->
                SwapResult.StableTokensLessThanBridgingFee(result.actualInDollars, result.bridgingFee)
            is Adapter.CrossChainSwapResult.StableTokensLessThanMin ->
                SwapResult.StableTokensLessThanMin(result.actualInDollars, result.minInDollars)
            is Adapter.CrossChainSwapResult.Success ->
                SwapResult.Success(result.trade)
            is Adapter.CrossChainSwapResult.TradeNotFound ->
                SwapResult.TradeNotFound
        }
    }

    sealed interface AllowedRangeResult {
        // trade not found or it is single network trade
        object NoLimit : AllowedRangeResult
        data class Limited(val min: TokenAmount, val max: TokenAmount) : AllowedRangeResult
    }

    suspend fun getAllowedRangeForInputToken(
        inputToken: DecimalsToken,
        outputNetwork: Network,
        slippageTolerance: Percentage = Percentage(0.07.bn)
    ): AllowedRangeResult {
        if (inputToken.network.chainId == outputNetwork.chainId) return AllowedRangeResult.NoLimit
        requireChainsSupported(inputToken.network, outputNetwork)
        return adapter.getAllowedRangeForInput(inputToken, outputNetwork, slippageTolerance)
    }

    private fun requireChainsSupported(inputNetwork: Network, outputNetwork: Network) =
        require(isChainsSupported(inputNetwork, outputNetwork)) {
            "You cannot make trade between $inputNetwork and $outputNetwork. " +
                    "Please use isChainsSupported(...) to check this"
        }

    fun isChainsSupported(inputNetwork: Network, outputNetwork: Network): Boolean =
        isChainsSupported(inputNetwork.chainId, outputNetwork.chainId)

    fun isChainsSupported(inputNetworkChainId: BigInt, outputNetworkChainId: BigInt): Boolean =
        when (inputNetworkChainId) {
            outputNetworkChainId -> true
            else -> adapter.isCrossChainSupported(inputNetworkChainId, outputNetworkChainId)
        }

    interface Adapter {
        sealed interface SingleNetworkSwapResult {
            class Success(val trade: UnifiedSwapTrade.SingleNetwork) : SingleNetworkSwapResult
            object TradeNotFound : SingleNetworkSwapResult
        }
        suspend fun findSingleNetworkTrade(
            amountIn: TokenAmount,
            tokens: NetworkTokenPair,
            slippageTolerance: Percentage,
            from: EthereumAddress,
            recipient: EthereumAddress
        ): SingleNetworkSwapResult

        sealed interface CrossChainSwapResult {
            class Success(val trade: UnifiedSwapTrade.CrossChain) : CrossChainSwapResult
            class StableTokensLessThanBridgingFee(val actualInDollars: BigNum, val bridgingFee: TokenAmount) : CrossChainSwapResult
            class StableTokensLessThanMin(val actualInDollars: BigNum, val minInDollars: BigNum) : CrossChainSwapResult
            class StableTokensGreaterThanMax(val actualInDollars: BigNum, val maxInDollars: BigNum) : CrossChainSwapResult
            object TradeNotFound : CrossChainSwapResult
        }

        /**
         * Assume that cross chain supported
         */
        suspend fun findCrossChainTrade(
            amountIn: TokenAmount,
            tokens: TokenPair,
            slippageTolerance: Percentage,
            from: EthereumAddress,
            recipient: EthereumAddress
        ): CrossChainSwapResult

        /**
         * Assume that cross chain supported.
         * @return [AllowedRangeResult.NoLimit] if trade not found.
         */
        suspend fun getAllowedRangeForInput(
            inputToken: DecimalsToken,
            outputNetwork: Network,
            slippageTolerance: Percentage
        ): AllowedRangeResult

        fun isCrossChainSupported(inputNetworkChainId: BigInt, outputNetworkChainId: BigInt): Boolean
    }
}
