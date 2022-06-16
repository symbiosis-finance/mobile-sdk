package com.symbiosis.sdk.swap.unified

import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.swap.crosschain.CrossChainSwapTrade
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTrade

sealed interface UnifiedPath {
    val tokens: List<DecimalsToken>

    interface CrossChain : UnifiedPath {
        val inputPath: CrossChainPathTrade
        val stablePath: CrossChainPathTrade
        val outputPath: CrossChainPathTrade

        class Default(crossChain: CrossChainSwapTrade) : CrossChain {
            override val inputPath = CrossChainPathTrade.Default(crossChain.inputTrade)
            override val stablePath = CrossChainPathTrade.StableDefault(crossChain.stableTrade)
            override val outputPath = CrossChainPathTrade.Default(crossChain.outputTrade)

            // omit stable path here cuz of duplicated tokens
            override val tokens = inputPath.tokens + outputPath.tokens

            override fun toString(): String {
                return "CrossChainPath(inputPath=$inputPath, stablePath=$stablePath, outputPath=$outputPath)"
            }
        }
    }

    interface SingleNetwork : UnifiedPath {
        class Default(underlying: SingleNetworkTrade) : SingleNetwork {
            override val tokens = underlying.path

            override fun toString(): String {
                return "SingleNetworkPath(tokens=$tokens)"
            }
        }
    }
}
