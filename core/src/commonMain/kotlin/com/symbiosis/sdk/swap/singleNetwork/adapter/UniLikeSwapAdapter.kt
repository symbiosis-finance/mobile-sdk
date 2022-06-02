package com.symbiosis.sdk.swap.singleNetwork.adapter

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkSwapRepository
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkSwapRepository.ExactInResult
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkSwapRepository.ExactOutResult
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTokenPair
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTrade
import com.symbiosis.sdk.swap.singleNetwork.asNetworkTokenPair
import com.symbiosis.sdk.swap.uni.UniLikeSwapRepository
import dev.icerock.moko.web3.EthereumAddress

class UniLikeSwapAdapter(
    private val uniLike: UniLikeSwapRepository
) : SingleNetworkSwapRepository.ExactInAdapter,
    SingleNetworkSwapRepository.ExactOutAdapter {

    override suspend fun exactIn(
        amountIn: BigInt,
        tokens: SingleNetworkTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress
    ): ExactInResult {
        val trade = when (val tradeResult = uniLike.exactIn(amountIn, tokens.asNetworkTokenPair)) {
            is UniLikeSwapRepository.ExactInResult.TradeNotFound ->
                return ExactInResult.TradeNotFound
            is UniLikeSwapRepository.ExactInResult.Success ->
                tradeResult.trade
        }

        val callData = trade.callData(
            slippageTolerance,
            recipient
        )

        val wrapped = SingleNetworkTrade.UniLike.ExactIn(
            underlying = trade,
            slippageTolerance = slippageTolerance,
            recipient = recipient,
            callData = callData
        )

        return ExactInResult.Success(wrapped)
    }

    override suspend fun exactOut(
        amountOut: BigInt,
        tokens: SingleNetworkTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress
    ): ExactOutResult {
        val trade = when (val tradeResult = uniLike.exactOut(amountOut, tokens.asNetworkTokenPair)) {
            is UniLikeSwapRepository.ExactOutResult.TradeNotFound ->
                return ExactOutResult.TradeNotFound
            is UniLikeSwapRepository.ExactOutResult.InsufficientLiquidity ->
                return ExactOutResult.InsufficientLiquidity
            is UniLikeSwapRepository.ExactOutResult.Success ->
                tradeResult.trade
        }

        val callData = trade.callData(slippageTolerance, recipient)

        val wrapped = SingleNetworkTrade.UniLike.ExactOut(
            underlying = trade,
            slippageTolerance = slippageTolerance,
            recipient = recipient,
            callData = callData
        )

        return ExactOutResult.Success(wrapped)
    }

}
