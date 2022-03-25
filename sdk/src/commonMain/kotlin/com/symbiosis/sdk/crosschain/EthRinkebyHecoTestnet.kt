package com.symbiosis.sdk.crosschain

import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.networks.HecoTestnet
import com.symbiosis.sdk.swap.meta.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class EthRinkebyHecoTestnet(
    ethRinkebyExecutor: Web3Executor,
    hecoTestnetExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(ethRinkebyUrl: String, hecoTestnetUrl: String) :
            this(Web3(ethRinkebyUrl), Web3(hecoTestnetUrl))

    override val fromNetwork = EthRinkeby(ethRinkebyExecutor)
    override val toNetwork = HecoTestnet(hecoTestnetExecutor)
    override val stablePool: NerveStablePool = StablePools.HUOBI_HUSD_sUSDC_POOL(toNetwork, fromNetwork)
}
