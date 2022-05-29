package com.symbiosis.sdk.swap.crosschain.bridging

import com.symbiosis.sdk.ClientsManager
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.swap.crosschain.CrossChain
import com.symbiosis.sdk.swap.crosschain.SingleNetworkSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.StableSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.fromToken
import com.symbiosis.sdk.swap.crosschain.getSynthSwapTokens
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.hex.HexString

internal abstract class AbsBridgingFeeProviderAdapter(
    protected val crossChain: CrossChain
): DefaultBridgingFeeProvider.Adapter {
    protected val inputNetwork = crossChain.fromNetwork
    protected val inputNetworkClient = ClientsManager.getNetworkClient(inputNetwork)

    protected val outputNetwork = crossChain.toNetwork
    protected val outputNetworkClient = ClientsManager.getNetworkClient(outputNetwork)
}

internal class BurnBridgingFeeProviderAdapter(crossChain: CrossChain) : AbsBridgingFeeProviderAdapter(crossChain) {
    override val receiveSide: ContractAddress = outputNetwork.portalAddress

    override suspend fun callData(
        tokens: TokenPair,
        inputTrade: SingleNetworkSwapTradeAdapter,
        stableTrade: StableSwapTradeAdapter,
        outputTrade: SingleNetworkSwapTradeAdapter,
        recipient: EthereumAddress
    ): HexString =
        inputNetworkClient
            .portal
            .getMetaUnsynthesizeCalldata(
                token = crossChain.stablePool.tokens.last().tokenAddress,
                amount = stableTrade.amountOutEstimated,
                to = recipient,
                synthesisRequestsCount = inputNetworkClient.synthesize.requestsCount(),
                finalNetwork = outputNetwork,
                finalSwapCalldata = outputTrade.callData,
                finalOffset = outputTrade.callDataOffset
            )
}

internal class SynthBridgingFeeProviderAdapter(crossChain: CrossChain) : AbsBridgingFeeProviderAdapter(crossChain) {
    override val receiveSide: ContractAddress = outputNetwork.synthesizeAddress

    override suspend fun callData(
        tokens: TokenPair,
        inputTrade: SingleNetworkSwapTradeAdapter,
        stableTrade: StableSwapTradeAdapter,
        outputTrade: SingleNetworkSwapTradeAdapter,
        recipient: EthereumAddress
    ): HexString = inputNetworkClient
        .synthesize
        .getMetaMintSyntheticTokenCalldata(
            to = recipient,
            portalRequestsCount = inputNetworkClient.portal.requestsCount(),
            finalNetwork = outputNetwork,
            finalSwapCallData = outputTrade.callData,
            finalSwapOffset = outputTrade.callDataOffset,
            swapTokens = getSynthSwapTokens(stableTrade, outputTrade, tokens.second),
            stableSwapCallData = stableTrade.callData(deadline = null),
            stablePoolAddress = crossChain.stablePool.address,
            firstSwapAmountOut = inputTrade.amountOutEstimated,
            firstStableToken = crossChain.fromToken.tokenAddress
        )
}
