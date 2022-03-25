package com.symbiosis.sdk.crosschain

import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.networks.PolygonMumbai
import com.symbiosis.sdk.swap.meta.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class PolygonMumbaiEthRinkeby(
    polygonMumbaiExecutor: Web3Executor,
    ethRinkebyExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(polygonMumbaiUrl: String, ethRinkebyUrl: String) :
            this(Web3(polygonMumbaiUrl), Web3(ethRinkebyUrl))

    override val fromNetwork = PolygonMumbai(polygonMumbaiExecutor)
    override val toNetwork = EthRinkeby(ethRinkebyExecutor)
    override val stablePool: NerveStablePool = StablePools.MUMBAI_USDT_sUSDC_POOL(fromNetwork, toNetwork)
}
