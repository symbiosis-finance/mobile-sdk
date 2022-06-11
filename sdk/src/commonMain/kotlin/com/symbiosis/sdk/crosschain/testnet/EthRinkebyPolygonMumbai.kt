package com.symbiosis.sdk.crosschain.testnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.networks.PolygonMumbai
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class EthRinkebyPolygonMumbai(
    ethRinkebyExecutor: Web3Executor,
    polygonMumbaiExecutor: Web3Executor
) : TestnetCrossChain() {
    constructor(ethRinkebyUrl: String, polygonMumbaiUrl: String) :
            this(Web3(ethRinkebyUrl), Web3(polygonMumbaiUrl))

    override val fromNetwork = EthRinkeby(ethRinkebyExecutor)
    override val toNetwork = PolygonMumbai(polygonMumbaiExecutor)
    override val stablePool: NerveStablePool =
        StablePools.POLYGON_MUMBAI_USDT_ETH_RINKEBY_sUSDC_POOL(toNetwork, fromNetwork)
}
