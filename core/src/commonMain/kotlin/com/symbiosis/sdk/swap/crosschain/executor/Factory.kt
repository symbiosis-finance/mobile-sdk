package com.symbiosis.sdk.swap.crosschain.executor

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.NativeToken
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.swap.crosschain.CrossChain
import com.symbiosis.sdk.swap.crosschain.SingleNetworkSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.StableSwapTradeAdapter
import dev.icerock.moko.web3.EthereumAddress

fun CrossChainTradeExecutorAdapter(
    crossChain: CrossChain,
    inputTrade: SingleNetworkSwapTradeAdapter,
    stableTrade: StableSwapTradeAdapter,
    outputTrade: SingleNetworkSwapTradeAdapter,
    tokens: TokenPair,
    bridgingFee: BigInt,
    fromAddress: EthereumAddress,
    recipient: EthereumAddress,
    amountIn: TokenAmount
): CrossChainTradeExecutorAdapter {
    val directionAdapter = when (crossChain.hasPoolOnFirstNetwork) {
        true -> BurnCrossChainExecutorDirectionAdapter(
            inputTrade, stableTrade, outputTrade, tokens,
            crossChain, bridgingFee, fromAddress, recipient
        )
        false -> SynthCrossChainExecutorDirectionAdapter(
            tokens, inputTrade, stableTrade, outputTrade,
            crossChain, bridgingFee, fromAddress, recipient
        )
    }

    return DefaultCrossChainTradeExecutorAdapter(
        crossChain, inputTrade, stableTrade, directionAdapter,
        nativeIn = tokens.first is NativeToken,
        tokens = tokens,
        amountIn = amountIn
    )
}
