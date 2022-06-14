package com.symbiosis.sdk.crosschain

import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.configuration.SwapTTLProvider
import com.symbiosis.sdk.providers.DefaultTTLProvider
import com.symbiosis.sdk.swap.crosschain.CrossChain

abstract class DefaultCrossChain : CrossChain {
    override val ttlProvider: SwapTTLProvider = DefaultTTLProvider
    override val minStableTokensAmountPerTrade: BigNum = 10.bn
    override val maxStableTokensAmountPerTrade: BigNum = 10_000.bn
}
