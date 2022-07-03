package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.AvalancheMainnet
import com.symbiosis.sdk.networks.EthMainnet
import dev.icerock.moko.web3.Web3Executor

class AvalancheMainnetEthMainnet(
    avalancheMainnetExecutor: Web3Executor,
    ethMainnetExecutor: Web3Executor
) : MainnetCrossChain() {
    override val fromNetwork = AvalancheMainnet(avalancheMainnetExecutor)
    override val toNetwork = EthMainnet(ethMainnetExecutor)
    override val stablePool = StablePools.AVALANCHE_MAINNET_USDC_ETH_MAINNET_sUSDC(fromNetwork, toNetwork)
}
