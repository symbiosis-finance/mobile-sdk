package com.symbiosis.sdk.crosschain

import com.symbiosis.sdk.networks.AvalancheFuji
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.swap.meta.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class AvalancheFujiBscTestnet(
    avalancheFujiExecutor: Web3Executor,
    bscTestnetExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(avalancheFujiUrl: String, bscTestnetUrl: String) :
            this(Web3(avalancheFujiUrl), Web3(bscTestnetUrl))

    override val fromNetwork = AvalancheFuji(avalancheFujiExecutor)
    override val toNetwork = BscTestnet(bscTestnetExecutor)
    override val stablePool: NerveStablePool = StablePools.FUJI_USDT_sBUSD_POOL(fromNetwork, toNetwork)
}
