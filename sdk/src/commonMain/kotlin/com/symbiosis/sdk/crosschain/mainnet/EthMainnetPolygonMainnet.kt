package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.EthMainnet
import com.symbiosis.sdk.networks.PolygonMainnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class EthMainnetPolygonMainnet(
    ethMainnetExecutor: Web3Executor,
    polygonMainnetExecutor: Web3Executor
) : MainnetCrossChain() {
    override val fromNetwork = EthMainnet(ethMainnetExecutor)
    override val toNetwork = PolygonMainnet(polygonMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.POLYGON_MAINNET_USDC_ETH_MAINNET_sUSDC(toNetwork, fromNetwork)
}
