package com.symbiosis.sdk.swap.singleNetwork

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.swap.Percentage
import dev.icerock.moko.web3.entity.EthereumAddress

class SingleNetworkSwapRepository(adapters: List<Adapter>) {

    init {
        require(adapters.any { it is ExactInAdapter })
        require(adapters.any { it is ExactOutAdapter })
    }

    private val exactInAdapter =
        adapters.filterIsInstance<ExactInAdapter>()
            .reduce { accAdapter, adapter -> accAdapter + adapter  }
    private val exactOutAdapter =
        adapters.filterIsInstance<ExactOutAdapter>()
            .reduce { accAdapter, adapter -> accAdapter + adapter }

    suspend fun exactIn(
        amountIn: TokenAmount,
        tokens: SingleNetworkTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress = from
    ): ExactInResult = exactInAdapter.exactIn(amountIn, tokens, slippageTolerance, from, recipient)

    suspend fun exactOut(
        amountOut: BigInt,
        tokens: SingleNetworkTokenPair,
        slippageTolerance: Percentage,
        from: EthereumAddress,
        recipient: EthereumAddress = from
    ): ExactOutResult = exactOutAdapter.exactOut(amountOut, tokens, slippageTolerance, from, recipient)

    sealed interface Adapter

    sealed interface ExactInResult {
        class Success(val trade: SingleNetworkTrade.ExactIn) : ExactInResult
        object TradeNotFound : ExactInResult
    }

    interface ExactInAdapter : Adapter {
        suspend fun exactIn(
            amountIn: TokenAmount,
            tokens: SingleNetworkTokenPair,
            slippageTolerance: Percentage,
            from: EthereumAddress,
            recipient: EthereumAddress = from
        ): ExactInResult

        operator fun plus(other: ExactInAdapter): ExactInAdapter =
            ComposedExactInAdapter(first = this, second = other)
    }

    sealed interface ExactOutResult {
        class Success(val trade: SingleNetworkTrade.ExactOut) : ExactOutResult
        object TradeNotFound : ExactOutResult
        object InsufficientLiquidity : ExactOutResult
    }

    interface ExactOutAdapter : Adapter {
        suspend fun exactOut(
            amountOut: BigInt,
            tokens: SingleNetworkTokenPair,
            slippageTolerance: Percentage,
            from: EthereumAddress,
            recipient: EthereumAddress = from
        ): ExactOutResult

        operator fun plus(other: ExactOutAdapter): ExactOutAdapter =
            ComposedExactOutAdapter(first = this, second = other)
    }
}
