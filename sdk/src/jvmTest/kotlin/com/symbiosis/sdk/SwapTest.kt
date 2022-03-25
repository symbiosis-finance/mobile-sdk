package com.symbiosis.sdk

import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.network.contract.getSyntheticToken
import com.symbiosis.sdk.swap.CalculatedSwapTrade
import com.symbiosis.sdk.swap.SwapRoutesGenerator
import com.symbiosis.sdk.swap.SwapType
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
import kotlin.test.assertTrue

class SwapTest {
    private val sdk = ClientsManager()

    //    @Test
    fun optimizedSwap() = runBlocking {
        println(SwapRoutesGenerator.getBaseRoutes(network = testETH))

        val pair = NetworkTokenPair(
            first = testETH.token.UNI,
            second = testETH.token.WETH
        )
        sdk.getNetworkClient(testETH).also { client ->
            val trade = client.swap.findBestTrade(
                pair, "4000000000000000000".bi, SwapType.ExactOut
            ).first
                ?: error("Swap trade not found")

            trade as? CalculatedSwapTrade.ExactOut.Success
                ?: error("Insufficient liquidity")

            val txHash = client.swap.execute(
                credentials = alexCredentials,
                trade = trade
            ).prefixed
            println(txHash)
        }
    }

//        @Test
    fun swapDemo() = runBlocking {
        val value = TokenAmount(1.bn, decimals = 18).raw
        sdk.getNetworkClient(testETH).also { client ->
            val sWBNB = client.synthFabric.getSyntheticToken(testBSC.token.WBNB)
                ?: error("Synthetic was not found")

            val trade = client.swap.findBestTradeExactIn(
                networkTokenPair = NetworkTokenPair(
                    testETH.token.UNI,
                    sWBNB
                ),
                amountIn = value
            ).first ?: error("Trade was not found")

            client.swap.execute(
                credentials = alexCredentials,
                trade = trade
            )
            client.synthesize.burnSynthTokens(
                credentials = alexCredentials,
                amount = trade.amountOut,
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
            val value = TokenAmount(1.bn, decimals = 18).raw
            sdk.getNetworkClient(testBSC).also { client ->
                val firstToken = testBSC.token.CAKE
                val secondToken = testBSC.token.BUSD

                val trade = client.swap.findBestTradeExactIn(
                    networkTokenPair = NetworkTokenPair(
                        firstToken,
                        secondToken
                    ),
                    amountIn = value
                ).first ?: error("Trade was not found")
                print(trade)

                val hash = client.swap.execute(
                    credentials = markCredentials,
                    trade = trade
                )
                print(hash)

                return@runBlocking
            }
            assertTrue { true }
        }
    }
}
