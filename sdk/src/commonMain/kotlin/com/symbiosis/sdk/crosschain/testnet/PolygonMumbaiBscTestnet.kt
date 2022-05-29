package com.symbiosis.sdk.crosschain.testnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.networks.PolygonMumbai
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class PolygonMumbaiBscTestnet(
    polygonMumbaiExecutor: Web3Executor,
    bscTestnetExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(polygonMumbaiUrl: String, bscTestnetUrl: String) :
            this(Web3(polygonMumbaiUrl), Web3(bscTestnetUrl))

    override val fromNetwork = PolygonMumbai(polygonMumbaiExecutor)
    override val toNetwork = BscTestnet(bscTestnetExecutor)
    override val stablePool: NerveStablePool =
        StablePools.POLYGON_MUMBAI_USDT_BSC_TESTNET_sBUSD_POOL(fromNetwork, toNetwork)
}
