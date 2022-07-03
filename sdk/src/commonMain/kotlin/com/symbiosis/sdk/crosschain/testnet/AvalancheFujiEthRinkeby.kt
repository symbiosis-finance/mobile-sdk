package com.symbiosis.sdk.crosschain.testnet

import com.symbiosis.sdk.crosschain.DefaultCrossChain
import com.symbiosis.sdk.crosschain.StablePools
import com.symbiosis.sdk.networks.AvalancheFuji
import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.swap.crosschain.NerveStablePool
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class AvalancheFujiEthRinkeby(
    avalancheFujiExecutor: Web3Executor,
    ethRinkebyExecutor: Web3Executor
) : TestnetCrossChain() {
    override val fromNetwork = AvalancheFuji(avalancheFujiExecutor)
    override val toNetwork = EthRinkeby(ethRinkebyExecutor)
    override val stablePool: NerveStablePool =
        StablePools.AVALANCHE_FUJI_USDT_ETH_RINKEBY_sUSDC_POOL(fromNetwork, toNetwork)
}
