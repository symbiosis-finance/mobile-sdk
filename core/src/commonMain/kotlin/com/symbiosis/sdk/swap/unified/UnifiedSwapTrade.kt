package com.symbiosis.sdk.swap.unified

import com.soywiz.kbignum.BigNum
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
    val priceImpact: UnifiedPriceImpact
    val path: UnifiedPath
    val fees: UnifiedFees

    sealed interface ExecuteResult<out T : UnifiedSwapTransaction> {
        class Success<out T : UnifiedSwapTransaction>(val transaction: T) : ExecuteResult<T>

        // Failed while estimating
        object ExecutionRevertedWithoutSending : ExecuteResult<Nothing>
    }

    suspend fun execute(credentials: Credentials, gasProvider: GasProvider? = null): ExecuteResult<*>

    interface SingleNetwork : UnifiedSwapTrade {
        override val tokens: NetworkTokenPair
        override val priceImpact: UnifiedPriceImpact.SingleNetwork
        override val path: UnifiedPath.SingleNetwork
        override val fees: UnifiedFees.SingleNetwork

        override suspend fun execute(
            credentials: Credentials,
            gasProvider: GasProvider?
        ): ExecuteResult<UnifiedSwapTransaction.SingleNetwork>

        data class Default(val underlying: SingleNetworkTrade.ExactIn) : SingleNetwork {
            override val amountIn = underlying.amountIn
            override val amountOutEstimated = underlying.amountOutEstimated
            override val amountOutMin = underlying.amountOutMin
            override val tokens = underlying.tokens
            override val priceImpact = UnifiedPriceImpact.SingleNetwork.Default(underlying)
            override val path = UnifiedPath.SingleNetwork.Default(underlying)
            override val fees = UnifiedFees.SingleNetwork.Default(underlying)

            override suspend fun execute(
                credentials: Credentials, gasProvider: GasProvider?
            ): ExecuteResult<UnifiedSwapTransaction.SingleNetwork> {
                val web3Transaction = underlying.execute(credentials, gasProvider)
                return UnifiedSwapTransaction.SingleNetwork.Default(web3Transaction)
                    .let { tx -> ExecuteResult.Success(tx) }
            }
        }
    }
    interface CrossChain : UnifiedSwapTrade {
        override val priceImpact: UnifiedPriceImpact.CrossChain
        override val path: UnifiedPath.CrossChain
        override val fees: UnifiedFees.CrossChain

        val dollarsAmount: BigNum

        override suspend fun execute(
            credentials: Credentials,
            gasProvider: GasProvider?
        ): ExecuteResult<UnifiedSwapTransaction.CrossChain>

        data class Default(val underlying: CrossChainSwapTrade) : CrossChain {
            override val amountIn: TokenAmount = underlying.amountIn
            override val amountOutEstimated: TokenAmount = underlying.amountOutEstimated
            override val amountOutMin: TokenAmount = underlying.amountOutMin
            override val tokens: TokenPair = underlying.tokens
            override val priceImpact = UnifiedPriceImpact.CrossChain.Default(underlying)
            override val path = UnifiedPath.CrossChain.Default(underlying)
            override val fees = UnifiedFees.CrossChain.Default(underlying)

            override val dollarsAmount = underlying.dollarsAmount

            override suspend fun execute(
                credentials: Credentials,
                gasProvider: GasProvider?
            ): ExecuteResult<UnifiedSwapTransaction.CrossChain> {
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
