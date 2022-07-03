package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.AvalancheMainnet
import com.symbiosis.sdk.networks.BscMainnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3Executor

class AvalancheMainnetBscMainnet(
    avalancheMainnetExecutor: Web3Executor,
    bscMainnetExecutor: Web3Executor
) : MainnetCrossChain() {
    override val fromNetwork = AvalancheMainnet(avalancheMainnetExecutor)
    override val toNetwork = BscMainnet(bscMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.AVALANCHE_MAINNET_USDC_BSC_MAINNET_sBUSD(fromNetwork, toNetwork)
}
