package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BscMainnet
import com.symbiosis.sdk.networks.PolygonMainnet
import com.symbiosis.sdk.swap.meta.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class PolygonMainnetBscMainnet(
    polygonMainnetExecutor: Web3Executor,
    bscMainnetExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(polygonMainnetUrl: String, bscMainnetUrl: String) :
            this(Web3(polygonMainnetUrl), Web3(bscMainnetUrl))

    override val fromNetwork = PolygonMainnet(polygonMainnetExecutor)
    override val toNetwork = BscMainnet(bscMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.POLYGON_MAINNET_USDC_BSC_MAINNET_sBUSD(fromNetwork, toNetwork)
}
