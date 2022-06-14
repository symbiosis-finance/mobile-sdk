package com.symbiosis.sdk.swap.unified

import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.crosschain.CrossChainSwapTrade
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTrade

sealed interface UnifiedPriceImpact {
    val total: Percentage

    interface CrossChain : UnifiedPriceImpact {
        val inputTradePriceImpact: Percentage
        val stableTradePriceImpact: Percentage
        val outputTradePriceImpact: Percentage

        class Default(crossChain: CrossChainSwapTrade) : CrossChain {
            override val inputTradePriceImpact = crossChain.priceImpact.inputTade
            override val stableTradePriceImpact = crossChain.priceImpact.stableTrade
            override val outputTradePriceImpact = crossChain.priceImpact.outputTrade
            override val total = crossChain.priceImpact.total
            override fun toString(): String {
                return "CrossChainPriceImpact(inputPriceImpact=$inputTradePriceImpact, stablePriceImpact=$stableTradePriceImpact, outputPriceImpact=$outputTradePriceImpact, total=$total)"
            }
        }
    }

    interface SingleNetwork : UnifiedPriceImpact {
        class Default(singleNetwork: SingleNetworkTrade.ExactIn) : SingleNetwork {
            override val total = singleNetwork.priceImpact
            override fun toString(): String {
                return "SingleNetworkPriceImpact(total=$total)"
            }
        }
    }
}
