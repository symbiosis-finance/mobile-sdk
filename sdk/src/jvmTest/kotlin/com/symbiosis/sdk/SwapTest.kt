package com.symbiosis.sdk

import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.TokenAmountConverter
import com.symbiosis.sdk.currency.asDecimalsToken
import com.symbiosis.sdk.network.contract.getSyntheticToken
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.uni.UniLikeSwapRepository
import com.symbiosis.sdk.swap.uni.UniLikeSwapRoutesGenerator
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
import kotlin.test.Test

class SwapTest {

    @Test
    fun testPaths() {
        println(
            UniLikeSwapRoutesGenerator.getRoutes(
            networkPair = NetworkTokenPair(
                first = testETH.token.ETH,
                second = testETH.token.UNI
            )
        ))
    }

    //    @Test
    fun optimizedUniLikeSwap() = runBlocking {
        println(UniLikeSwapRoutesGenerator.getBaseRoutes(network = testETH))

        val pair = NetworkTokenPair(
            first = testETH.token.UNI,
            second = testETH.token.WETH
        )
        testETH.symbiosisClient.also { client ->
            val tradeResult = client.uniLike.exactOut(
                amountOut = "4000000000000000000".bi,
                tokens = pair
            )

            if (tradeResult !is UniLikeSwapRepository.ExactOutResult.Success)
                error("Trade not found")

            println(tradeResult.trade.execute(alexCredentials, slippageTolerance = Percentage("0.07".bn)))
        }
    }

//        @Test
    fun swapDemo() = runBlocking {
        val value = TokenAmountConverter(1.bn, decimals = 18).raw
        testETH.symbiosisClient.also { client ->
            val sWBNB = client.networkClient.synthFabric.getSyntheticToken(testBSC.token.WBNB)
                ?: error("Synthetic was not found")

            val tradeResult = client.uniLike.exactIn(
                tokens = NetworkTokenPair(
                    testETH.token.UNI,
                    sWBNB.asDecimalsToken(testBSC.token.WBNB.decimals)
                ),
                amountIn = value
            )

            if (tradeResult !is UniLikeSwapRepository.ExactInResult.Success)
                error("Trade not found")

            tradeResult.trade.execute(
                credentials = alexCredentials,
                slippageTolerance = Percentage("0.07".bn)
            )

            client.networkClient.synthesize.burnSynthTokens(
                credentials = alexCredentials,
                amount = tradeResult.trade.amountOutEstimated.raw,
                synthCurrencyAddress = sWBNB.tokenAddress,
                targetNetwork = testETH,
                stableBridgingFee = 0.bi
            )

            return@runBlocking
        }
    }

    @Ignore
    //@Test
    fun `Swap Demo Ios`() {
        runBlocking {
            val value = TokenAmountConverter(1.bn, decimals = 18).raw
            testBSC.symbiosisClient.also { client ->
                val firstToken = testBSC.token.CAKE
                val secondToken = testBSC.token.BUSD

                val tradeResult = client.uniLike.exactIn(
                    tokens = NetworkTokenPair(
                        firstToken,
                        secondToken
                    ),
                    amountIn = value
                )

                if (tradeResult !is UniLikeSwapRepository.ExactInResult.Success)
                    error("Trade not found")

                val trade = tradeResult.trade

                print(tradeResult)

                val hash = trade.execute(
                    credentials = markCredentials,
                    slippageTolerance = Percentage("0.07".bn)
                )

                print(hash)

                return@runBlocking
            }
        }
    }
}
