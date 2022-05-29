package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BscMainnet
import com.symbiosis.sdk.networks.PolygonMainnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BscMainnetPolygonMainnet(
    bscMainnetExecutor: Web3Executor,
    polygonMainnetExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(bscMainnetUrl: String, polygonMainnetUrl: String) :
            this(Web3(bscMainnetUrl), Web3(polygonMainnetUrl))

    override val fromNetwork = BscMainnet(bscMainnetExecutor)
    override val toNetwork = PolygonMainnet(polygonMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.POLYGON_MAINNET_USDC_BSC_MAINNET_sBUSD(toNetwork, fromNetwork)
}
