package com.symbiosis.sdk.crosschain

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
    override val stablePool: NerveStablePool = StablePools.BUSD_sUSDC_POOL(toNetwork, fromNetwork)
}
