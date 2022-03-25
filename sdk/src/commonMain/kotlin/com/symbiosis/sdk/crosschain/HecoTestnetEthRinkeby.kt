package com.symbiosis.sdk.crosschain

import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.networks.HecoTestnet
import com.symbiosis.sdk.swap.meta.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class HecoTestnetEthRinkeby(
    hecoTestnetExecutor: Web3Executor,
    ethRinkebyExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(hecoTestnetUrl: String, ethRinkebyUrl: String) :
            this(Web3(hecoTestnetUrl), Web3(ethRinkebyUrl))

    override val fromNetwork = HecoTestnet(hecoTestnetExecutor)
    override val toNetwork = EthRinkeby(ethRinkebyExecutor)
    override val stablePool: NerveStablePool = StablePools.HUOBI_HUSD_sUSDC_POOL(fromNetwork, toNetwork)
}
