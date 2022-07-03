package com.symbiosis.sdk.swap.crosschain

import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.currency.thisOrWrapped
import dev.icerock.moko.web3.entity.ContractAddress

internal fun getSynthSwapTokens(
    stableTrade: StableSwapTradeAdapter,
    outputTrade: SingleNetworkSwapTradeAdapter,
    outputToken: DecimalsToken
): List<ContractAddress> = listOf(
    stableTrade.synthToken.tokenAddress,
    stableTrade.tokens.second.tokenAddress
).let { list ->
    when (outputTrade is SingleNetworkSwapTradeAdapter.Empty) {
        true -> list
        false -> list + outputToken.thisOrWrapped.tokenAddress
    }
}
