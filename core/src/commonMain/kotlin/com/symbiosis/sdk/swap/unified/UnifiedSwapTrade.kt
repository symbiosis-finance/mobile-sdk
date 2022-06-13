package com.symbiosis.sdk.swap.unified

import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.swap.crosschain.CrossChainSwapTrade
import com.symbiosis.sdk.swap.crosschain.executor.CrossChainTradeExecutorAdapter
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTrade
import com.symbiosis.sdk.wallet.Credentials

sealed interface UnifiedSwapTrade {
    val amountIn: TokenAmount
    val amountOutEstimated: TokenAmount
    val amountOutMin: TokenAmount
    val tokens: TokenPair

    sealed interface ExecuteResult {
        class Success(val transaction: UnifiedSwapTransaction) : ExecuteResult

        // Failed while estimating
        object ExecutionRevertedWithoutSending : ExecuteResult
    }

    suspend fun execute(credentials: Credentials, gasProvider: GasProvider? = null): ExecuteResult

    interface SingleNetwork : UnifiedSwapTrade {
        override val tokens: NetworkTokenPair

        data class Default(val underlying: SingleNetworkTrade.ExactIn) : SingleNetwork {
            override val amountIn = underlying.amountIn
            override val amountOutEstimated = underlying.amountOutEstimated
            override val amountOutMin = underlying.amountOutMin
            override val tokens = underlying.tokens

            override suspend fun execute(
                credentials: Credentials, gasProvider: GasProvider?
            ): ExecuteResult {
                val web3Transaction = underlying.execute(credentials, gasProvider)
                return UnifiedSwapTransaction.SingleNetwork.Default(web3Transaction)
                    .let(ExecuteResult::Success)
            }
        }
    }
    interface CrossChain : UnifiedSwapTrade {
        data class Default(val underlying: CrossChainSwapTrade) : CrossChain {
            override val amountIn: TokenAmount = underlying.amountIn
            override val amountOutEstimated: TokenAmount = underlying.amountOutEstimated
            override val amountOutMin: TokenAmount = underlying.amountOutMin
            override val tokens: TokenPair = underlying.tokens

            override suspend fun execute(
                credentials: Credentials,
                gasProvider: GasProvider?
            ): ExecuteResult {
                val crossChainTransaction = when (
                    val result = underlying.execute(credentials, gasProvider = gasProvider)
                ) {
                    is CrossChainTradeExecutorAdapter.ExecuteResult.ExecutionRevertedWithoutSending ->
                        return ExecuteResult.ExecutionRevertedWithoutSending
                    is CrossChainTradeExecutorAdapter.ExecuteResult.Sent ->
                        result.transaction
                }

                return ExecuteResult.Success(UnifiedSwapTransaction.CrossChain.Default(crossChainTransaction))
            }
        }
    }
}
