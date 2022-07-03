package com.symbiosis.sdk.crosschain.testnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.AvalancheFuji
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BscTestnetAvalancheFuji(
    bscTestnetExecutor: Web3Executor,
    avalancheFujiExecutor: Web3Executor
) : TestnetCrossChain() {
    override val fromNetwork = BscTestnet(bscTestnetExecutor)
    override val toNetwork = AvalancheFuji(avalancheFujiExecutor)
    override val stablePool: NerveStablePool =
        StablePools.AVALANCHE_FUJI_USDT_BSC_TESTNET_sBUSD_POOL(toNetwork, fromNetwork)
}
