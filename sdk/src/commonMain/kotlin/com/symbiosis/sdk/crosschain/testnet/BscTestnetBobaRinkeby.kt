package com.symbiosis.sdk.crosschain.testnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BobaRinkeby
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BscTestnetBobaRinkeby(
    bscTestnetExecutor: Web3Executor,
    bobaRinkebyExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(bscTestnetUrl: String, bobaRinkebyUrl: String) :
            this(Web3(bscTestnetUrl), Web3(bobaRinkebyUrl))

    override val fromNetwork = BscTestnet(bscTestnetExecutor)
    override val toNetwork = BobaRinkeby(bobaRinkebyExecutor)
    override val stablePool: NerveStablePool =
        StablePools.BOBA_RINKEBY_USDC_BSC_TESTNET_sBUSD_POOL(toNetwork, fromNetwork)
}
