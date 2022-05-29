package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BobaMainnet
import com.symbiosis.sdk.networks.BscMainnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BscMainnetBobaMainnet(
    bscMainnetExecutor: Web3Executor,
    bobaMainnetExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(bscMainnetUrl: String, bobaMainnetUrl: String) :
            this(Web3(bscMainnetUrl), Web3(bobaMainnetUrl))

    override val fromNetwork = BscMainnet(bscMainnetExecutor)
    override val toNetwork = BobaMainnet(bobaMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.BOBA_MAINNET_USDC_BSC_MAINNET_sBUSD(toNetwork, fromNetwork)
}
