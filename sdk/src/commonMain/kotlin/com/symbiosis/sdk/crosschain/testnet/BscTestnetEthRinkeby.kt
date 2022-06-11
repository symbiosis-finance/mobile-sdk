package com.symbiosis.sdk.crosschain.testnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BscTestnetEthRinkeby(
    bscTestnetExecutor: Web3Executor,
    ethRinkebyExecutor: Web3Executor
) : TestnetCrossChain() {
    constructor(bscTestnetUrl: String, ethRinkebyUrl: String) :
            this(Web3(bscTestnetUrl), Web3(ethRinkebyUrl))

    override val fromNetwork = BscTestnet(bscTestnetExecutor)
    override val toNetwork = EthRinkeby(ethRinkebyExecutor)
    override val stablePool: NerveStablePool =
        StablePools.BSC_TESTNET_BUSD_ETH_RINKEBY_sUSDC_POOL(fromNetwork, toNetwork)
}
