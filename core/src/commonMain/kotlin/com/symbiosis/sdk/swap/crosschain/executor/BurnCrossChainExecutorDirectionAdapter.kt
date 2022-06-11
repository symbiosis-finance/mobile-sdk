package com.symbiosis.sdk.swap.crosschain.executor

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.AddressZero
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.network.networkClient
import com.symbiosis.sdk.swap.crosschain.CrossChain
import com.symbiosis.sdk.swap.crosschain.CrossChainTokenPair
import com.symbiosis.sdk.swap.crosschain.SingleNetworkSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.StableSwapTradeAdapter
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.hex.HexString

class BurnCrossChainExecutorDirectionAdapter(
    private val inputTrade: SingleNetworkSwapTradeAdapter,
    private val stableTrade: StableSwapTradeAdapter,
    private val outputTrade: SingleNetworkSwapTradeAdapter,
    private val tokens: CrossChainTokenPair,
    private val crossChain: CrossChain,
    private val bridgingFee: BigInt,
    private val fromAddress: EthereumAddress,
    private val recipient: EthereumAddress
) : DefaultCrossChainTradeExecutorAdapter.DirectionAdapter {

    override suspend fun secondSwapCallData(deadline: BigInt?): HexString =
        stableTrade.callData(deadline)

    override val approvedTokens: List<ContractAddress> =
        listOf(
            (tokens.first as? Erc20Token)?.tokenAddress ?: AddressZero,
            stableTrade.route.first.tokenAddress,
            stableTrade.synthToken.tokenAddress
        )

    override val relayRecipient: ContractAddress =
        crossChain.fromNetwork.synthesizeAddress

    override suspend fun otherSideCallData(deadline: BigInt?): HexString =
        crossChain.fromNetwork
            .networkClient
            .synthesize
            .getMetaBurnSynthTokenCalldata(
                stableBridgingFee = bridgingFee,
                amount = stableTrade.amountOutEstimated,
                fromAddress = fromAddress,
                finalSwapCallData = outputTrade.callData,
                finalOffset = outputTrade.callDataOffset,
                finalDexRouter = outputTrade.routerAddress,
                sToken = stableTrade.synthToken.tokenAddress,
                chain2Address = recipient,
                receiveSide = crossChain.toNetwork.portalAddress,
                oppositeBridge = crossChain.toNetwork.bridgeAddress,
                chainId = crossChain.toNetwork.chainId
            )
}
