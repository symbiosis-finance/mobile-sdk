package com.symbiosis.sdk.swap.unified

import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.swap.crosschain.CrossChainSwapTrade
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTrade

sealed interface UnifiedFees {
    interface CrossChain : UnifiedFees {
        val inputTradeFee: TokenAmount
        val bridgingFee: TokenAmount
        val outputTradeFee: TokenAmount

        class Default(crossChain: CrossChainSwapTrade) : CrossChain {
            override val inputTradeFee = crossChain.fee.inputTrade
            override val bridgingFee = crossChain.fee.bridgingFee
            override val outputTradeFee = crossChain.fee.outputTrade
            override fun toString(): String {
                return "CrossChainFees(inputTradeFee=$inputTradeFee, bridgingFee=$bridgingFee, outputTradeFee=$outputTradeFee)"
            }
        }
    }
    interface SingleNetwork : UnifiedFees {
        val fee: TokenAmount

        class Default(singleNetwork: SingleNetworkTrade.ExactIn) : SingleNetwork {
            override val fee = singleNetwork.fee
            override fun toString(): String {
                return "SingleNetworkFees(fee=$fee)"
            }
        }
    }
}
