package com.symbiosis.sdk.swap.crosschain

import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.currency.thisOrWrapped
import dev.icerock.moko.web3.ContractAddress

internal fun getSynthSwapTokens(
    stableTrade: StableSwapTradeAdapter,
    outputTrade: SingleNetworkSwapTradeAdapter,
    outputToken: Token
): List<ContractAddress> = listOf(
    stableTrade.synthToken.tokenAddress,
    stableTrade.tokens.second.tokenAddress
).let { list ->
    when (outputTrade is SingleNetworkSwapTradeAdapter.Empty) {
        true -> list
        false -> list + outputToken.thisOrWrapped.tokenAddress
    }
}
