package com.symbiosis.sdk.crosschain.testnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BobaRinkeby
import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BobaRinkebyEthRinkeby(
    bobaRinkebyExecutor: Web3Executor,
    ethRinkebyExecutor: Web3Executor
) : TestnetCrossChain() {
    constructor(bobaRinkebyUrl: String, ethRinkebyUrl: String) :
            this(Web3(bobaRinkebyUrl), Web3(ethRinkebyUrl))

    override val fromNetwork = BobaRinkeby(bobaRinkebyExecutor)
    override val toNetwork = EthRinkeby(ethRinkebyExecutor)
    override val stablePool: NerveStablePool =
        StablePools.BOBA_RINKEBY_USDC_ETH_RINKEBY_sUSDC_POOL(fromNetwork, toNetwork)
}
