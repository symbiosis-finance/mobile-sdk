package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain

abstract class MainnetCrossChain : DefaultCrossChain() {
    override val advisorUrl: String = "https://api.symbiosis.finance/calculations/v1/swap/price"
}
