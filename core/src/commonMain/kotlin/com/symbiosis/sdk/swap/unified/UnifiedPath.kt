package com.symbiosis.sdk.swap.unified

import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.swap.crosschain.CrossChainSwapTrade
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTrade

sealed interface UnifiedPath {
    val tokens: List<DecimalsToken>

    interface CrossChain : UnifiedPath {
        val inputPath: List<DecimalsToken>
        val stablePath: List<DecimalsToken>
        val outputPath: List<DecimalsToken>

        class Default(crossChain: CrossChainSwapTrade) : CrossChain {
            override val inputPath = crossChain.inputTrade.path
            override val stablePath = listOf(crossChain.stableTrade.tokens.first, crossChain.stableTrade.tokens.second)
            override val outputPath = crossChain.outputTrade.path

            // omit stable path here cuz of duplicated tokens
            override val tokens = inputPath + outputPath

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
