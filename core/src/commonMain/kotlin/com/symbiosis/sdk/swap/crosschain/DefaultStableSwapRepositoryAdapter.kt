package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.SwapTTLProvider
import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.internal.time.timeMillis
import com.symbiosis.sdk.network.networkClient
import com.symbiosis.sdk.swap.crosschain.nerve.NerveSwapRepository

class DefaultStableSwapRepositoryAdapter(
    private val nerveSwapRepository: NerveSwapRepository,
    private val swapTTLProvider: SwapTTLProvider,
    private val chainId: BigInt
) : StableSwapRepositoryAdapter {
    override suspend fun findBestTrade(amountIn: TokenAmount): StableSwapTradeAdapter {
        val trade = nerveSwapRepository.findTrade(amountIn)

        val route =
            when (trade.crossChain.hasPoolOnFirstNetwork) {
                true -> {
                    val synthFabric = trade
                        .crossChain
                        .fromNetwork
                        .networkClient
                        .synthFabric

                    trade.crossChain.stablePool.getPoolRoute(synthFabric)
                }
                false -> {
                    val synthFabric = trade
                        .crossChain
                        .toNetwork
                        .networkClient
                        .synthFabric

                    trade.crossChain.stablePool.getPoolRoute(synthFabric)
                }
            }

        return StableSwapTradeAdapter.Default(
            trade,
            trade.crossChain.stablePool.getTargetTokenSynth(
                trade.crossChain.fromNetwork.networkClient.synthFabric,
                trade.crossChain.toNetwork.networkClient.synthFabric
            ).let { token ->
                DecimalsErc20Token(token.network, token.tokenAddress, trade.crossChain.stablePool.targetToken.decimals)
            },
            route,
            defaultDeadlineProvider = {
                timeMillis.bi + swapTTLProvider.getSwapTTL(chainId)
            }
        )
    }
}
