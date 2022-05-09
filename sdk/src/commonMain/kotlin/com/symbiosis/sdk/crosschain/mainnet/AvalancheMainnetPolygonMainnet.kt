package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.AvalancheMainnet
import com.symbiosis.sdk.networks.PolygonMainnet
import com.symbiosis.sdk.swap.meta.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class AvalancheMainnetPolygonMainnet(
    avalancheMainnetExecutor: Web3Executor,
    polygonMainnetExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(avalancheMainnetUrl: String, polygonMainnetUrl: String) :
            this(Web3(avalancheMainnetUrl), Web3(polygonMainnetUrl))

    override val fromNetwork = AvalancheMainnet(avalancheMainnetExecutor)
    override val toNetwork = PolygonMainnet(polygonMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.POLYGON_MAINNET_USDC_AVALANCHE_MAINNET_sUSDC(toNetwork, fromNetwork)
}
