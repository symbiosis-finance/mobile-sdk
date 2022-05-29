package com.symbiosis.sdk.swap.crosschain

import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.swap.crosschain.nerve.NerveTokenPair

class StableSwapTokenPair(
    override val first: Erc20Token,
    override val second: Erc20Token,
) : TokenPair by TokenPair(first, second)

val NerveTokenPair.asStableSwap get() = StableSwapTokenPair(first, second)
