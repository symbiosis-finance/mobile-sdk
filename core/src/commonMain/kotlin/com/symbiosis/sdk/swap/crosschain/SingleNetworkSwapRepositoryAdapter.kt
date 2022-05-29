package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.swap.Percentage
import dev.icerock.moko.web3.EthereumAddress

interface SingleNetworkSwapRepositoryAdapter {
    suspend fun exactIn(
        amountIn: BigInt,
        tokens: NetworkTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress
    ): CrossChainSwapRepository.Adapter.ExactInResult
}
