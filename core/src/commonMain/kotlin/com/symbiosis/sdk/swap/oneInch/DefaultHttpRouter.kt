package com.symbiosis.sdk.swap.oneInch

import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.oneInch.priceImpact.OneInchPriceImpactRepository
import dev.icerock.moko.web3.EthereumAddress

class DefaultHttpRouter(
    private val networkClient: NetworkClient,
    private val network: OneInchSwapRepository.Network
) : OneInchSwapRepository.Router {
    private val oneInchClient = OneInchHttpClient(networkClient.network)

    private val priceImpactRepository = OneInchPriceImpactRepository(networkClient, network)

    override suspend fun findBestTrade(
        tokens: OneInchTokenPair,
        amountIn: TokenAmount,
        slippageTolerance: Percentage,
        fromAddress: EthereumAddress,
        recipient: EthereumAddress
    ): OneInchSwapRepository.ExactInResult {
        require(slippageTolerance >= 0 && slippageTolerance <= 50)

        val swapResult = oneInchClient.swap(
            fromTokenAddress = tokens.first.address,
            toTokenAddress = tokens.second.address,
            amount = amountIn.raw,
            fromAddress = fromAddress,
            slippageTolerance = slippageTolerance,
            destReceiver = recipient
        )

        val swapResponse = when (swapResult) {
            is OneInchHttpClient.SwapResult.Success -> swapResult.swapResponse
            OneInchHttpClient.SwapResult.InsufficientLiquidity ->
                return OneInchSwapRepository.ExactInResult.InsufficientLiquidity
            OneInchHttpClient.SwapResult.NotEnoughEthForGas ->
                error("All checks disabled, so unreachable")
        }

        val priceImpact: Percentage = priceImpactRepository
            .priceImpact(tokens, amountIn.raw, swapResponse.toTokenAmount)

        val trade = OneInchTrade(
            client = networkClient,
            oneInchClient = oneInchClient,
            tokens = tokens,
            amountIn = amountIn,
            amountOutEstimated = TokenAmount(swapResponse.toTokenAmount, tokens.second.asToken(networkClient.network)),
            callData = swapResponse.tx.data,
            to = swapResponse.tx.to,
            value = swapResponse.tx.value,
            gasPrice = swapResponse.tx.gasPrice,
            gasLimit = swapResponse.tx.gas,
            slippageTolerance = slippageTolerance,
            recipient = recipient,
            fromAddress = fromAddress,
            priceImpact = priceImpact
        )

        return OneInchSwapRepository.ExactInResult.Success(trade)
    }
}
