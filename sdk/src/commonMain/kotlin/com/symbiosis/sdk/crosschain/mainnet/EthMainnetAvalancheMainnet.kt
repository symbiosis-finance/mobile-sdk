package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.AvalancheMainnet
import com.symbiosis.sdk.networks.EthMainnet
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class EthMainnetAvalancheMainnet(
    ethMainnetExecutor: Web3Executor,
    avalancheMainnetExecutor: Web3Executor
) : MainnetCrossChain() {
    override val fromNetwork = EthMainnet(ethMainnetExecutor)
    override val toNetwork = AvalancheMainnet(avalancheMainnetExecutor)
    override val stablePool = StablePools.AVALANCHE_MAINNET_USDC_ETH_MAINNET_sUSDC(toNetwork, fromNetwork)
}
