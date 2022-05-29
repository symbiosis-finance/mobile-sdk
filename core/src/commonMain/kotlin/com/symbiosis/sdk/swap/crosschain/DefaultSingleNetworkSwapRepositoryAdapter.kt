package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.crosschain.CrossChainSwapRepository.Adapter.ExactInResult
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkSwapRepository
import dev.icerock.moko.web3.EthereumAddress

class DefaultSingleNetworkSwapRepositoryAdapter(
    private val singleNetworkSwapRepository: SingleNetworkSwapRepository
) : SingleNetworkSwapRepositoryAdapter {
    override suspend fun exactIn(
        amountIn: BigInt,
        tokens: NetworkTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress
    ): ExactInResult {
        val trade = when (
            val result = singleNetworkSwapRepository.exactIn(
                amountIn, tokens, slippageTolerance,
                from, recipient
            )
        ) {
            is SingleNetworkSwapRepository.ExactInResult.Success -> result.trade
            is SingleNetworkSwapRepository.ExactInResult.TradeNotFound -> return ExactInResult.TradeNotFound
        }

        val wrapped = SingleNetworkSwapTradeAdapter.Default(trade, slippageTolerance)

        return ExactInResult.Success(wrapped)
    }
}
