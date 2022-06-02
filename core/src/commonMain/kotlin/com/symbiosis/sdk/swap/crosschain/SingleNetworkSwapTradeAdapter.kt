package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.AddressZero
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTrade
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.hex.HexString

sealed interface SingleNetworkSwapTradeAdapter {
    val amountIn: BigInt
    val amountOutEstimated: BigInt
    val amountOutMin: BigInt
    val priceImpact: Percentage
    val fee: BigInt
    val callData: HexString?
    val routerAddress: ContractAddress

    /**
     * @return null if input token is native
     */
    val firstTokenAddress: ContractAddress?

    // https://github.com/symbiosis-finance/js-sdk/blob/main/src/crosschain/oneInchTrade.ts#L84
    val callDataOffset: BigInt

    data class Default(
        val underlying: SingleNetworkTrade.ExactIn,
        val slippageTolerance: Percentage
    ) : SingleNetworkSwapTradeAdapter {
        override val amountIn: BigInt = underlying.amountIn
        override val amountOutEstimated: BigInt = underlying.amountOutEstimated
        override val priceImpact: Percentage = underlying.priceImpact
        override val fee: BigInt = underlying.fee
        override val callData: HexString = underlying.callData
        override val amountOutMin: BigInt = underlying.amountOutMin
        override val routerAddress: ContractAddress = underlying.routerAddress
        override val callDataOffset: BigInt = underlying.callDataOffset
        override val firstTokenAddress: ContractAddress? = (underlying.tokens.first as? Erc20Token)
            ?.tokenAddress
    }

    data class Empty(
        override val amountIn: BigInt,
        override val firstTokenAddress: ContractAddress?,
    ) : SingleNetworkSwapTradeAdapter {
        override val amountOutEstimated = amountIn
        override val priceImpact = Percentage(0.bn)
        override val fee = 0.bi
        override val callData: HexString? = null
        override val amountOutMin: BigInt = amountIn
        override val routerAddress: ContractAddress = AddressZero
        override val callDataOffset: BigInt = 0.bi
    }
}
