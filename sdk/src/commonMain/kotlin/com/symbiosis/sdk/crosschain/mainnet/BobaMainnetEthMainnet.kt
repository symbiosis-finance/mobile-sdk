package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BobaMainnet
import com.symbiosis.sdk.networks.EthMainnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BobaMainnetEthMainnet(
    bobaMainnetExecutor: Web3Executor,
    ethMainnetExecutor: Web3Executor
) : MainnetCrossChain() {
    constructor(bobaMainnetUrl: String, ethMainnetUrl: String) :
            this(Web3(bobaMainnetUrl), Web3(ethMainnetUrl))

    override val fromNetwork = BobaMainnet(bobaMainnetExecutor)
    override val toNetwork = EthMainnet(ethMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.BOBA_MAINNET_USDC_ETH_MAINNET_sUSDC(fromNetwork, toNetwork)
}
