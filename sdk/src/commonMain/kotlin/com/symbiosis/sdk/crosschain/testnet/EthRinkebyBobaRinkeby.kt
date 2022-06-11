package com.symbiosis.sdk.crosschain.testnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BobaRinkeby
import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class EthRinkebyBobaRinkeby(
    ethRinkebyExecutor: Web3Executor,
    bobaRinkebyExecutor: Web3Executor
) : TestnetCrossChain() {
    constructor(ethRinkebyUrl: String, bobaRinkebyUrl: String) :
            this(Web3(ethRinkebyUrl), Web3(bobaRinkebyUrl))

    override val fromNetwork = EthRinkeby(ethRinkebyExecutor)
    override val toNetwork = BobaRinkeby(bobaRinkebyExecutor)
    override val stablePool: NerveStablePool =
        StablePools.BOBA_RINKEBY_USDC_ETH_RINKEBY_sUSDC_POOL(toNetwork, fromNetwork)
}
