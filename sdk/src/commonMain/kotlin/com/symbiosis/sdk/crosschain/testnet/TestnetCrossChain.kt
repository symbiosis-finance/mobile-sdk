package com.symbiosis.sdk.crosschain.testnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain

abstract class TestnetCrossChain : DefaultCrossChain() {
    override val advisorUrl: String = "https://api.dev.symbiosis.finance/calculations/v1/swap/price"
}
