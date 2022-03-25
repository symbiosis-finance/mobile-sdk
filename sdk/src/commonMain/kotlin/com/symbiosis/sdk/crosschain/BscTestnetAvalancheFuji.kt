package com.symbiosis.sdk.crosschain

import com.symbiosis.sdk.networks.AvalancheFuji
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.swap.meta.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BscTestnetAvalancheFuji(
    bscTestnetExecutor: Web3Executor,
    avalancheFujiExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(bscTestnetUrl: String, avalancheFujiUrl: String) :
            this(Web3(bscTestnetUrl), Web3(avalancheFujiUrl))

    override val fromNetwork = BscTestnet(bscTestnetExecutor)
    override val toNetwork = AvalancheFuji(avalancheFujiExecutor)
    override val stablePool: NerveStablePool = StablePools.FUJI_USDT_sBUSD_POOL(toNetwork, fromNetwork)
}
