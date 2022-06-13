package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.AddressZero
import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.DecimalsNativeToken
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkTrade
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.hex.HexString

sealed interface SingleNetworkSwapTradeAdapter {
    val amountIn: TokenAmount
    val amountOutEstimated: TokenAmount
    val amountOutMin: TokenAmount
    val priceImpact: Percentage
    val fee: TokenAmount
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
        override val amountIn = underlying.amountIn
        override val amountOutEstimated = underlying.amountOutEstimated
        override val priceImpact: Percentage = underlying.priceImpact
        override val fee: TokenAmount = underlying.fee
        override val callData: HexString = underlying.callData
        override val amountOutMin = underlying.amountOutMin
        override val routerAddress: ContractAddress = underlying.routerAddress
        override val callDataOffset: BigInt = underlying.callDataOffset
        override val firstTokenAddress: ContractAddress? = (underlying.tokens.first as? Erc20Token)
            ?.tokenAddress
    }

    data class Empty(
        override val amountIn: TokenAmount,
    ) : SingleNetworkSwapTradeAdapter {
        override val firstTokenAddress: ContractAddress? =
            when (amountIn.token) {
                is DecimalsErc20Token -> amountIn.token.tokenAddress
                is DecimalsNativeToken -> null
            }
        override val amountOutEstimated = amountIn
        override val priceImpact = Percentage(0.bn)
        override val fee = TokenAmount(0.bi, amountIn.token.network.nativeCurrency)
        override val callData: HexString? = null
        override val amountOutMin = amountIn
        override val routerAddress: ContractAddress = AddressZero
        override val callDataOffset: BigInt = 0.bi
    }
}
