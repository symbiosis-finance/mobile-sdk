package com.symbiosis.sdk.crosschain.testnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BobaRinkeby
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BobaRinkebyBscTestnet(
    bobaRinkebyExecutor: Web3Executor,
    bscTestnetExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(bobaRinkebyUrl: String, bscTestnetUrl: String) :
            this(Web3(bobaRinkebyUrl), Web3(bscTestnetUrl))

    override val fromNetwork = BobaRinkeby(bobaRinkebyExecutor)
    override val toNetwork = BscTestnet(bscTestnetExecutor)
    override val stablePool: NerveStablePool =
        StablePools.BOBA_RINKEBY_USDC_BSC_TESTNET_sBUSD_POOL(fromNetwork, toNetwork)
}
