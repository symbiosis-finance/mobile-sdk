package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.AvalancheMainnet
import com.symbiosis.sdk.networks.PolygonMainnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class PolygonMainnetAvalancheMainnet(
    polygonMainnetExecutor: Web3Executor,
    avalancheMainnetExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(polygonMainnetUrl: String, avalancheMainnetUrl: String) :
            this(Web3(polygonMainnetUrl), Web3(avalancheMainnetUrl))

    override val fromNetwork = PolygonMainnet(polygonMainnetExecutor)
    override val toNetwork = AvalancheMainnet(avalancheMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.POLYGON_MAINNET_USDC_AVALANCHE_MAINNET_sUSDC(fromNetwork, toNetwork)
}
