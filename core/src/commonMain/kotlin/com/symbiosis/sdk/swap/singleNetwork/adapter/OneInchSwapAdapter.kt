package com.symbiosis.sdk.swap.singleNetwork.adapter

import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.oneInch.OneInchSwapRepository
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkSwapRepository
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkSwapRepository.ExactInResult
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTokenPair
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTrade
import com.symbiosis.sdk.swap.singleNetwork.asOneInchPair
import dev.icerock.moko.web3.entity.EthereumAddress

class OneInchSwapAdapter(private val oneInch: OneInchSwapRepository) : SingleNetworkSwapRepository.ExactInAdapter {
    override suspend fun exactIn(
        amountIn: TokenAmount,
        tokens: SingleNetworkTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress
    ): ExactInResult {
        val trade = when (
            val tradeResult = oneInch.exactIn(amountIn, tokens.asOneInchPair, slippageTolerance, from, recipient)
        ) {
            is OneInchSwapRepository.ExactInResult.Success -> tradeResult.trade
            OneInchSwapRepository.ExactInResult.InsufficientLiquidity -> return ExactInResult.TradeNotFound
        }

        val wrapped = SingleNetworkTrade.OneInch(trade, oneInch, tokens.network)

        return ExactInResult.Success(wrapped)
    }
}
