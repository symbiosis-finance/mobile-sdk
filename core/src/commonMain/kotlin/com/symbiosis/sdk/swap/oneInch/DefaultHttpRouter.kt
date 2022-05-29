package com.symbiosis.sdk.swap.oneInch

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.swap.Percentage
import dev.icerock.moko.web3.EthereumAddress

class DefaultHttpRouter(private val networkClient: NetworkClient) : OneInchSwapRepository.Router {
    private val oneInchClient = OneInchHttpClient(networkClient.network)

    override suspend fun findBestTrade(
        tokens: OneInchTokenPair,
        amountIn: BigInt,
        slippageTolerance: Percentage,
        fromAddress: EthereumAddress,
        recipient: EthereumAddress
    ): OneInchSwapRepository.ExactInResult {
        require(slippageTolerance >= 0 && slippageTolerance <= 50)

        val swapResult = oneInchClient.swap(
            fromTokenAddress = tokens.first.address,
            toTokenAddress = tokens.second.address,
            amount = amountIn,
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

        val trade = OneInchTrade(
            client = networkClient,
            oneInchClient = oneInchClient,
            tokens = tokens,
            amountIn = amountIn,
            amountOutEstimated = swapResponse.toTokenAmount,
            callData = swapResponse.tx.data,
            to = swapResponse.tx.to,
            value = swapResponse.tx.value,
            gasPrice = swapResponse.tx.gasPrice,
            gasLimit = swapResponse.tx.gas,
            slippageTolerance = slippageTolerance,
            recipient = recipient,
            fromAddress = fromAddress
        )

        return OneInchSwapRepository.ExactInResult.Success(trade)
    }
}
