package com.symbiosis.sdk.swap.crosschain

import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.swap.crosschain.nerve.NerveTokenPair

class StableSwapTokenPairAdapter(
    override val first: DecimalsErc20Token,
    override val second: DecimalsErc20Token,
) : TokenPair by TokenPair(first, second)

val NerveTokenPair.asStableSwap get() = StableSwapTokenPairAdapter(first, second)
