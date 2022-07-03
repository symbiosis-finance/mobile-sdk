package com.symbiosis.sdk.crosschain.mainnet

import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.BobaMainnet
import com.symbiosis.sdk.networks.BscMainnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BobaMainnetBscMainnet(
    bobaMainnetExecutor: Web3Executor,
    bscMainnetExecutor: Web3Executor
) : MainnetCrossChain() {
    override val fromNetwork = BobaMainnet(bobaMainnetExecutor)
    override val toNetwork = BscMainnet(bscMainnetExecutor)
    override val stablePool: NerveStablePool = StablePools.BOBA_MAINNET_USDC_BSC_MAINNET_sBUSD(fromNetwork, toNetwork)
}
