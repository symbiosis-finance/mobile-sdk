package com.symbiosis.sdk.swap.oneInch

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.swap.Percentage
import dev.icerock.moko.web3.ContractAddress
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

    enum class Network(
        val chainId: BigInt,
        val oracleAddress: ContractAddress
    ) {
        Ethereum(
            chainId = 0x1,
            oracleAddress = "0x07D91f5fb9Bf7798734C3f606dB065549F6893bb"
        ),
        Binance(
            chainId = 0x38,
            oracleAddress = "0xfbD61B037C325b959c0F6A7e69D8f37770C2c550"
        ),
        Polygon(
            chainId = 0x89,
            oracleAddress = "0x7F069df72b7A39bCE9806e3AfaF579E54D8CF2b9"
        ),
        Optimizm(chainId = 0xA,
            oracleAddress = "0x11DEE30E710B8d4a8630392781Cc3c0046365d4c"
        ),
        Arbitrum(
            chainId = 0xA4B1,
            oracleAddress = "0x735247fb0a604c0adC6cab38ACE16D0DbA31295F"
        ),
        Gnosis(
            chainId = 0x64,
            oracleAddress = "0x142DB045195CEcaBe415161e1dF1CF0337A4d02E"
        ),
        Avalanche(
            chainId = 0xA86A,
            oracleAddress = "0xBd0c7AaF0bF082712EbE919a9dD94b2d978f79A9"
        );

        constructor(chainId: Int, oracleAddress: String) : this(chainId.bi, ContractAddress(oracleAddress))
    }
}
