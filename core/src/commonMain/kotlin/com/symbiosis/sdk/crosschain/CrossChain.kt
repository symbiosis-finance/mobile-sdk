package com.symbiosis.sdk.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.symbiosis.sdk.configuration.BridgingFeeProvider
import com.symbiosis.sdk.configuration.SwapTTLProvider
import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.swap.meta.NerveStablePool

interface CrossChain {
    val fromNetwork: Network
    val toNetwork: Network
    val stablePool: NerveStablePool
    val bridgingFeeProvider: BridgingFeeProvider
    val ttlProvider: SwapTTLProvider
    val minStableTokensAmountPerTrade: BigNum
    val maxStableTokensAmountPerTrade: BigNum
}

val CrossChain.reversed: CrossChain get() = object : CrossChain by this {
    override val fromNetwork: Network = this@reversed.toNetwork
    override val toNetwork: Network = this@reversed.fromNetwork
}

fun CrossChain.getStableTokenForNetwork(chainId: BigInt): DecimalsErc20Token {
    require(chainId in listOf(fromNetwork.chainId, toNetwork.chainId))

    return when (chainId) {
        stablePool.fromToken.network.chainId -> stablePool.fromToken
        stablePool.targetToken.network.chainId -> stablePool.targetToken
        else -> error("Unknown network")
    }
}

val CrossChain.fromToken: DecimalsErc20Token get() = getStableTokenForNetwork(fromNetwork.chainId)
val CrossChain.targetToken: DecimalsErc20Token get() = getStableTokenForNetwork(toNetwork.chainId)
