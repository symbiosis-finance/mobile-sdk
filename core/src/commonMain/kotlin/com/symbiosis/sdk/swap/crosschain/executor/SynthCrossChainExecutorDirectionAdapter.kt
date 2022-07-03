package com.symbiosis.sdk.swap.crosschain.executor

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.AddressZero
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.network.networkClient
import com.symbiosis.sdk.swap.crosschain.CrossChain
import com.symbiosis.sdk.swap.crosschain.SingleNetworkSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.StableSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.getSynthSwapTokens
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.hex.HexString

class SynthCrossChainExecutorDirectionAdapter(
    private val tokens: TokenPair,
    private val inputTrade: SingleNetworkSwapTradeAdapter,
    private val stableTrade: StableSwapTradeAdapter,
    private val outputTrade: SingleNetworkSwapTradeAdapter,
    private val crossChain: CrossChain,
    private val bridgingFee: BigInt,
    private val fromAddress: EthereumAddress,
    private val recipient: EthereumAddress
) : DefaultCrossChainTradeExecutorAdapter.DirectionAdapter {

    override suspend fun secondSwapCallData(deadline: BigInt?): HexString? = null

    override val approvedTokens: List<ContractAddress> =
        listOf(
            (tokens.first as? Erc20Token)?.tokenAddress ?: AddressZero,
            stableTrade.tokens.first.tokenAddress
        )

    override val relayRecipient: ContractAddress =
        crossChain.fromNetwork.portalAddress

    val swapTokens: List<ContractAddress> = getSynthSwapTokens(stableTrade, outputTrade, tokens.second)

    override suspend fun otherSideCallData(deadline: BigInt?): HexString =
        crossChain.fromNetwork
            .networkClient
            .portal
            .getMetaSynthesizeCalldata(
                stableBridgingFee = bridgingFee,
                amount = inputTrade.amountOutMin.raw,
                rtoken = stableTrade.tokens.first.tokenAddress,
                chain2address = recipient,
                receiveSide = crossChain.toNetwork.synthesizeAddress,
                oppositeBridge = crossChain.toNetwork.bridgeAddress,
                fromAddress = fromAddress,
                chainId = crossChain.toNetwork.chainId,
                swapTokens = swapTokens,
                secondDexRouter = stableTrade.routerAddress,
                secondSwapCalldata = stableTrade.callData(deadline),
                finalDexRouter = outputTrade.routerAddress,
                finalSwapCalldata = outputTrade.callData,
                finalOffset = outputTrade.callDataOffset
            )
}
