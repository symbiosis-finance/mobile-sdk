package com.symbiosis.sdk.swap.crosschain

import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.swap.Percentage
import dev.icerock.moko.web3.EthereumAddress

interface SingleNetworkSwapRepositoryAdapter {
    suspend fun exactIn(
        amountIn: TokenAmount,
        tokens: NetworkTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress
    ): CrossChainSwapRepository.Adapter.ExactInResult
}
