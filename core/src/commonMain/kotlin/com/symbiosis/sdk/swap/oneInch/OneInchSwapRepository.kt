package com.symbiosis.sdk.swap.oneInch

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.swap.Percentage
import dev.icerock.moko.web3.EthereumAddress

class OneInchSwapRepository(private val router: Router, val network: Network) {
    suspend fun exactIn(
        amountIn: BigInt,
        tokens: OneInchTokenPair,
        slippageTolerance: Percentage /* 0 to 50 */,
        fromAddress: EthereumAddress,
        recipient: EthereumAddress = fromAddress
    ): ExactInResult {
        return router
            .findBestTrade(tokens, amountIn, slippageTolerance, fromAddress, recipient)
    }

    interface Router {
        suspend fun findBestTrade(
            tokens: OneInchTokenPair,
            amountIn: BigInt,
            slippageTolerance: Percentage /* 0% to 50% */,
            fromAddress: EthereumAddress,
            recipient: EthereumAddress
        ): ExactInResult
    }

    sealed interface ExactInResult {
        class Success(val trade: OneInchTrade) : ExactInResult
        object InsufficientLiquidity : ExactInResult
    }

    enum class Network(val chainId: BigInt) {
        Ethereum(0x1), Binance(0x36), Polygon(0x89),
        Optimizm(0xA), Arbitrum(0xA4B1), Gnosis(0x64),
        Avalanche(0xA86A), Fantom(0xFA);

        constructor(chainId: Int) : this(chainId.bi)
    }
}
