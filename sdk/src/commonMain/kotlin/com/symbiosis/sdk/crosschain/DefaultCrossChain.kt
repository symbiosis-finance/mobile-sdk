package com.symbiosis.sdk.crosschain

import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.configuration.BridgingFeeProvider
import com.symbiosis.sdk.configuration.SwapTTLProvider
import com.symbiosis.sdk.providers.DefaultBridgingFeeProvider
import com.symbiosis.sdk.providers.DefaultTTLProvider

abstract class DefaultCrossChain : CrossChain {
    override val bridgingFeeProvider: BridgingFeeProvider = DefaultBridgingFeeProvider
    override val ttlProvider: SwapTTLProvider = DefaultTTLProvider
    override val minStableTokensAmountPerTrade: BigNum = 10.bn
    override val maxStableTokensAmountPerTrade: BigNum = 10_000.bn
}
