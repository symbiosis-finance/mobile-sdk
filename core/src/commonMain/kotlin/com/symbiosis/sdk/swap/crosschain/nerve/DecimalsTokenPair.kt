package com.symbiosis.sdk.swap.crosschain.nerve

import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.TokenPair

class NerveTokenPair(
    override val first: DecimalsErc20Token,
    override val second: DecimalsErc20Token
) : TokenPair by TokenPair(first, second)
