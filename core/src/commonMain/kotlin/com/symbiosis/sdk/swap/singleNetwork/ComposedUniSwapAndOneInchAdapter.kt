package com.symbiosis.sdk.swap.singleNetwork

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.singleNetwork.adapter.OneInchSwapAdapter
import com.symbiosis.sdk.swap.singleNetwork.adapter.UniLikeSwapAdapter
import dev.icerock.moko.web3.EthereumAddress

class ComposedUniSwapAndOneInchAdapter(
    val uniSwap: UniLikeSwapAdapter,
    val oneInchIfSupported: OneInchSwapAdapter?
) : SingleNetworkSwapRepository.ExactInAdapter, SingleNetworkSwapRepository.ExactOutAdapter {
    override suspend fun exactIn(
        amountIn: TokenAmount,
        tokens: SingleNetworkTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress
    ): SingleNetworkSwapRepository.ExactInResult  =
        when (val oneInch = oneInchIfSupported?.exactIn(amountIn, tokens, slippageTolerance, from, recipient)) {
            is SingleNetworkSwapRepository.ExactInResult.Success -> oneInch
            null, is SingleNetworkSwapRepository.ExactInResult.TradeNotFound ->
                uniSwap.exactIn(amountIn, tokens, slippageTolerance, from, recipient)
        }

    override suspend fun exactOut(
        amountOut: BigInt,
        tokens: SingleNetworkTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress
    ): SingleNetworkSwapRepository.ExactOutResult = uniSwap
        .exactOut(amountOut, tokens, slippageTolerance, from, recipient)
}
