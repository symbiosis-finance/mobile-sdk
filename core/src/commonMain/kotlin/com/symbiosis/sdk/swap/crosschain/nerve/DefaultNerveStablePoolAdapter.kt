package com.symbiosis.sdk.swap.crosschain.nerve

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.network.networkClient
import com.symbiosis.sdk.swap.crosschain.CrossChain
import com.symbiosis.sdk.swap.crosschain.fromToken
import com.symbiosis.sdk.swap.crosschain.targetToken

class DefaultNerveStablePoolAdapter(private val crossChain: CrossChain) : NerveSwapRepository.StablePool {
    private val tokenIndexFrom: BigInt =
        when (crossChain.fromNetwork.chainId) {
            crossChain.stablePool.fromNetwork.chainId -> 0
            else -> 1
        }.bi

    private val tokenIndexTo = 1.bi - tokenIndexFrom

    private val networkClient = crossChain
        .stablePool
        .fromNetwork
        .networkClient

    override suspend fun findTrade(amountIn: BigInt): NerveSwapTrade {
        val amountOutEstimated = when (amountIn) {
            0.bi -> 0.bi
            else -> networkClient
                .getNerveContract(crossChain.stablePool)
                .calculateSwap(tokenIndexFrom, tokenIndexTo, amountIn)
        }

        return NerveSwapTrade(
            amountIn = amountIn,
            amountOutEstimated = amountOutEstimated,
            crossChain = crossChain,
            networkClient = networkClient,
            tokenIndexFrom = tokenIndexFrom,
            tokenIndexTo = tokenIndexTo,
            tokens = NerveTokenPair(crossChain.fromToken, crossChain.targetToken)
        )
    }
}
