package com.symbiosis.sdk.crosschain

import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.networks.PolygonMumbai
import com.symbiosis.sdk.swap.meta.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BscTestnetPolygonMumbai(
    bscTestnetExecutor: Web3Executor,
    polygonMumbaiExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(bscTestnetUrl: String, polygonMumbaiUrl: String) :
            this(Web3(bscTestnetUrl), Web3(polygonMumbaiUrl))

    override val fromNetwork = BscTestnet(bscTestnetExecutor)
    override val toNetwork = PolygonMumbai(polygonMumbaiExecutor)
    override val stablePool: NerveStablePool = StablePools.MUMBAI_USDT_sBUSD_POOL(toNetwork, fromNetwork)
}
