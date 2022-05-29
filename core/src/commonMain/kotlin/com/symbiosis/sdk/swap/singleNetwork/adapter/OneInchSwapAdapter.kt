package com.symbiosis.sdk.swap.singleNetwork.adapter

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.oneInch.OneInchSwapRepository
import com.symbiosis.sdk.swap.oneInch.asOneInchPair
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkSwapRepository
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkSwapRepository.ExactInResult
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTrade
import dev.icerock.moko.web3.EthereumAddress

class OneInchSwapAdapter(private val oneInch: OneInchSwapRepository) : SingleNetworkSwapRepository.ExactInAdapter {
    override suspend fun exactIn(
        amountIn: BigInt,
        tokens: NetworkTokenPair,
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