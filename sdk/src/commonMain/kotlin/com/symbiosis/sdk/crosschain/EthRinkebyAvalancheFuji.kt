package com.symbiosis.sdk.crosschain

import com.symbiosis.sdk.networks.AvalancheFuji
import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.swap.meta.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class EthRinkebyAvalancheFuji(
    ethRinkebyExecutor: Web3Executor,
    avalancheFujiExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(ethRinkebyUrl: String, avalancheFujiUrl: String) :
            this(Web3(ethRinkebyUrl), Web3(avalancheFujiUrl))

    override val fromNetwork = EthRinkeby(ethRinkebyExecutor)
    override val toNetwork = AvalancheFuji(avalancheFujiExecutor)
    override val stablePool: NerveStablePool = StablePools.FUJI_USDT_sUSDC_POOL(toNetwork, fromNetwork)
}
