package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BobaMainnet
import com.symbiosis.sdk.networks.EthMainnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class EthMainnetBobaMainnet(
    ethMainnetExecutor: Web3Executor,
    bobaMainnetExecutor: Web3Executor
) : DefaultCrossChain() {
    constructor(ethMainnetUrl: String, bobaMainnetUrl: String) :
            this(Web3(ethMainnetUrl), Web3(bobaMainnetUrl))

    override val fromNetwork = EthMainnet(ethMainnetExecutor)
    override val toNetwork = BobaMainnet(bobaMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.BOBA_MAINNET_USDC_ETH_MAINNET_sUSDC(toNetwork, fromNetwork)
}
