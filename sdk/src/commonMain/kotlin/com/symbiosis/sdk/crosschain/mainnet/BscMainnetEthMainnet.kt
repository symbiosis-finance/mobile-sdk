package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BscMainnet
import com.symbiosis.sdk.networks.EthMainnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BscMainnetEthMainnet(
    bscMainnetExecutor: Web3Executor,
    ethMainnetExecutor: Web3Executor
) : MainnetCrossChain() {
    constructor(bscMainnetUrl: String, ethMainnetUrl: String) :
            this(Web3(bscMainnetUrl), Web3(ethMainnetUrl))

    override val fromNetwork = BscMainnet(bscMainnetExecutor)
    override val toNetwork = EthMainnet(ethMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.BSC_MAINNET_BUSD_ETH_MAINNET_sUSDC(fromNetwork, toNetwork)
}
