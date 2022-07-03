package com.symbiosis.sdk.crosschain.testnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.networks.PolygonMumbai
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BscTestnetPolygonMumbai(
    bscTestnetExecutor: Web3Executor,
    polygonMumbaiExecutor: Web3Executor
) : TestnetCrossChain() {
    override val fromNetwork = BscTestnet(bscTestnetExecutor)
    override val toNetwork = PolygonMumbai(polygonMumbaiExecutor)
    override val stablePool: NerveStablePool =
        StablePools.POLYGON_MUMBAI_USDT_BSC_TESTNET_sBUSD_POOL(toNetwork, fromNetwork)
}
