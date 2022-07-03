package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.AvalancheMainnet
import com.symbiosis.sdk.networks.BscMainnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BscMainnetAvalancheMainnet(
    bscMainnetExecutor: Web3Executor,
    avalancheMainnetExecutor: Web3Executor
) : MainnetCrossChain() {
    override val fromNetwork = BscMainnet(bscMainnetExecutor)
    override val toNetwork = AvalancheMainnet(avalancheMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.AVALANCHE_MAINNET_USDC_BSC_MAINNET_sBUSD(toNetwork, fromNetwork)
}
