package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.ClientsManager
import com.symbiosis.sdk.configuration.SwapTTLProvider
import com.symbiosis.sdk.internal.time.timeMillis
import com.symbiosis.sdk.swap.crosschain.nerve.NerveSwapRepository

class DefaultStableSwapRepositoryAdapter(
    private val nerveSwapRepository: NerveSwapRepository,
    private val swapTTLProvider: SwapTTLProvider,
    private val chainId: BigInt
) : StableSwapRepositoryAdapter {
    override suspend fun findBestTrade(amountIn: BigInt): StableSwapTradeAdapter {
        val trade = nerveSwapRepository.findTrade(amountIn)

        val route =
            when (trade.crossChain.hasPoolOnFirstNetwork) {
                true -> {
                    val synthFabric = ClientsManager
                        .getNetworkClient(trade.crossChain.fromNetwork)
                        .synthFabric

                    trade.crossChain.stablePool.getPoolRoute(synthFabric)
                }
                false -> {
                    val synthFabric = ClientsManager
                        .getNetworkClient(trade.crossChain.toNetwork)
                        .synthFabric

                    trade.crossChain.stablePool.getPoolRoute(synthFabric)
                }
            }

        return StableSwapTradeAdapter.Default(
            trade,
            trade.crossChain.stablePool.getTargetTokenSynth(
                ClientsManager.getNetworkClient(trade.crossChain.fromNetwork).synthFabric,
                ClientsManager.getNetworkClient(trade.crossChain.toNetwork).synthFabric
            ),
            route,
            defaultDeadlineProvider = {
                timeMillis.bi + swapTTLProvider.getSwapTTL(chainId)
            }
        )
    }
}
