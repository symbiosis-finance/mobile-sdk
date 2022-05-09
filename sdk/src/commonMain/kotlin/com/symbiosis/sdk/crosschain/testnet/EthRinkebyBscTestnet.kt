package com.symbiosis.sdk.crosschain.testnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.swap.meta.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class EthRinkebyBscTestnet(
    ethRinkebyExecutor: Web3Executor,
    bscTestnetExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(ethRinkebyUrl: String, bscTestnetUrl: String) :
            this(Web3(ethRinkebyUrl), Web3(bscTestnetUrl))

    override val fromNetwork = EthRinkeby(ethRinkebyExecutor)
    override val toNetwork = BscTestnet(bscTestnetExecutor)
    override val stablePool: NerveStablePool =
        StablePools.BSC_TESTNET_BUSD_ETH_RINKEBY_sUSDC_POOL(toNetwork, fromNetwork)
}
