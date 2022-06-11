package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BscMainnet
import com.symbiosis.sdk.networks.EthMainnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class EthMainnetBscMainnet(
    ethMainnetExecutor: Web3Executor,
    bscMainnetExecutor: Web3Executor
) : MainnetCrossChain() {
    constructor(ethMainnetUrl: String, bscMainnetUrl: String) :
            this(Web3(ethMainnetUrl), Web3(bscMainnetUrl))

    override val fromNetwork = EthMainnet(ethMainnetExecutor)
    override val toNetwork = BscMainnet(bscMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.BSC_MAINNET_BUSD_ETH_MAINNET_sUSDC(toNetwork, fromNetwork)
}
