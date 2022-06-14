package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.EthMainnet
import com.symbiosis.sdk.networks.PolygonMainnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class PolygonMainnetEthMainnet(
    polygonMainnetExecutor: Web3Executor,
    ethMainnetExecutor: Web3Executor
) : MainnetCrossChain() {
    constructor(polygonMainnetUrl: String, ethMainnetUrl: String) :
            this(Web3(polygonMainnetUrl), Web3(ethMainnetUrl))

    override val fromNetwork = PolygonMainnet(polygonMainnetExecutor)
    override val toNetwork = EthMainnet(ethMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.POLYGON_MAINNET_USDC_ETH_MAINNET_sUSDC(fromNetwork, toNetwork)
}

