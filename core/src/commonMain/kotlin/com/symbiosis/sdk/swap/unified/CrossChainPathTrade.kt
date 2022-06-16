package com.symbiosis.sdk.swap.unified

import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.swap.crosschain.SingleNetworkSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.StableSwapTradeAdapter

interface CrossChainPathTrade {
    val amountIn: TokenAmount
    val amountOutEstimated: TokenAmount
    val amountOutMin: TokenAmount

    val tokens: List<DecimalsToken>

    class Default(underlying: SingleNetworkSwapTradeAdapter) : CrossChainPathTrade {
        override val amountIn = underlying.amountIn
        override val amountOutEstimated = underlying.amountOutEstimated
        override val amountOutMin = underlying.amountOutMin
        override val tokens = underlying.path
    }

    class StableDefault(underlying: StableSwapTradeAdapter) : CrossChainPathTrade {
        override val amountIn = underlying.amountIn
        override val amountOutEstimated = underlying.amountOutEstimated
        override val amountOutMin = underlying.amountOutEstimated
        override val tokens: List<DecimalsToken> = underlying.tokens.asList()
    }
}
