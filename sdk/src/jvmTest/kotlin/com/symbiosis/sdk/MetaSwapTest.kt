package com.symbiosis.sdk

import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.crosschain.AvalancheFujiBscTestnet
import com.symbiosis.sdk.crosschain.BscTestnetEthRinkeby
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.convertIntegerToReal
import com.symbiosis.sdk.currency.convertRealToInteger
import com.symbiosis.sdk.internal.nonce.NonceController
import com.symbiosis.sdk.network.getTokenContract
import com.symbiosis.sdk.network.sendTransaction
import com.symbiosis.sdk.swap.meta.CalculatedMetaSwapTrade
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.requests.waitForTransactionReceipt
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

// fixme: add mocked tests
class MetaSwapTest {
    private val sdk = ClientsManager()

//    @Test
    fun metaSwapCalculation() {
        runBlocking {
            val fromToken = testBSC.token.CAKE
            val targetToken = testETH.token.UNI

            val metaSwap =
                sdk.getCrossChainClient(
                    crossChain = BscTestnetEthRinkeby(
                        bscTestnetExecutor = testBSC.executor,
                        ethRinkebyExecutor = testETH.executor
                    )
                )

            println(
                metaSwap.findBestTradeExactIn(
                    fromToken = fromToken,
                    targetToken = targetToken,
                    amountIn = 1_000_000_000_000_000_000.bi,
                    to = alexWalletAddress
                )
            )
        }
    }

    //@Test
    fun metaSwapUnified() {
        runBlocking {
            val fromToken: Erc20Token = testAvalanche.token.USDT
            val toToken: Erc20Token = testBSC.token.CAKE

            println(
                "balance of ${fromToken.tokenAddress} is ${
                    testETH.token.UNI.convertIntegerToReal(
                        sdk.getNetworkClient(testETH).getTokenContract(
                            fromToken
                        ).balanceOf(denWalletAddress)
                    )
                }"
            )

            val metaSwap = sdk.getCrossChainClient(
                crossChain = AvalancheFujiBscTestnet(
                    avalancheFujiExecutor = testAvalanche.executor,
                    bscTestnetExecutor = testBSC.executor
                )
            )

            val trade = metaSwap.findBestTradeExactIn(
                fromToken = fromToken,
                targetToken = toToken,
                amountIn = testAvalanche.token.USDT.convertRealToInteger("12".bn),
                to = denWalletAddress
            ) as CalculatedMetaSwapTrade.ExactIn? ?: error("Trade was not found")

            println("I will get ${testBSC.token.CAKE.convertIntegerToReal(trade.targetValueMin)}")

            val txHash = metaSwap.execute(
                credentials = denCredentials,
                trade = trade,
                gasProvider = testAvalanche.gasProvider
            )

            txHash.waitForReceipt()
        }
    }

    //    @Test
    fun testNervePoolExecution() {
        runBlocking {
            val nonceController = NonceController(testBSC.executor)
            val contractAddress1 = ContractAddress("0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D")
            val callData1 =
                "0x38ed1739000000000000000000000000000000000000000000000000000009184e72a0000000000000000000000000000000000000000000000000000aaf62911ce8c5b900000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000c43d2c472cf882e0b190063d66ee8ce78bf54da10000000000000000000000000000000000000000000000000000017d79f8acec000000000000000000000000000000000000000000000000000000000000000200000000000000000000000041b5984f45afb2560a0ed72bb69a98e8b32b3cca0000000000000000000000009a01bf917477dd9f5d715d188618fc8b7350cd22".let(
                    ::HexString
                )
            val txHash = nonceController.withNonce(alexWalletAddress) { nonce ->
                val signed = alexCredentials.signer.signContractTransaction(
                    nonce = nonce,
                    chainId = testBSC.chainId,
                    to = contractAddress1,
                    contractData = callData1.prefixed,
                    value = 0.bi,
                    gasConfiguration = testBSC.gasProvider.getGasConfiguration(
                        from = alexCredentials.address,
                        to = contractAddress1,
                        callData = callData1,
                        value = null,
                        executor = testBSC.executor
                    )
                )
                testBSC.executor.sendTransaction(signed)
            }
            testBSC.executor.waitForTransactionReceipt(txHash)

            sdk.getNetworkClient(testBSC).also { client ->
                with(client.getTokenContract(testBSC.token.BUSD)) {
                    balanceOf(alexWalletAddress).let(::println)
                    approveMax(
                        credentials = alexCredentials,
                        spender = ContractAddress("0xD99D1c33F9fC3444f8101754aBC46c52416550D1"),
                    )
                }
                delay(3000)
                val callData2 =
                    "0x91695586000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010000000000000000000000000000000000000000000000000b7d46336ee9c6d700000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000017d79f8bad4".let(
                        ::HexString
                    )
                val contractAddress2 = ContractAddress("0x6C4A94baDc1306539401789886a36E3DBd843B10")
                // sending nerve transaction
                val txHash2 = nonceController.withNonce(alexWalletAddress) { nonce ->
                    val signed = alexCredentials.signer.signContractTransaction(
                        nonce = nonce,
                        chainId = testBSC.chainId,
                        to = contractAddress2,
                        contractData = callData2.prefixed,
                        value = 0.bi,
                        gasConfiguration = testBSC.gasProvider.getGasConfiguration(
                            from = alexCredentials.address,
                            to = contractAddress2,
                            callData = callData2,
                            value = null,
                            executor = testBSC.executor
                        )
                    )
                    testBSC.executor.sendTransaction(signed)
                }
                testBSC.executor.waitForTransactionReceipt(txHash2)

                with(client.getTokenContract(ContractAddress("0x4188d0a4894f466b1903e67477ddf14196ea39d4"))) {
                    approveMax(
                        credentials = alexCredentials,
                        spender = ContractAddress("0x3D1a085E575f079Cacc88Df323FCA1EAC2764cc9"),
                    )
                    client.synthesize.burnSynthTokens(
                        credentials = alexCredentials,
                        amount = balanceOf(alexWalletAddress).also(::println),
                        synthCurrencyAddress = ContractAddress("0x4188D0A4894F466B1903E67477ddf14196ea39D4"),
                        targetNetwork = testETH,
                        stableBridgingFee = 0.bi
                    )
                }
            }

            with(
                sdk.getNetworkClient(testETH)
                    .getTokenContract(ContractAddress("0x4DBCdF9B62e891a7cec5A2568C3F4FAF9E8Abe2b"))
            ) {
                approveMax(
                    credentials = alexCredentials,
                    spender = ContractAddress("0xD99D1c33F9fC3444f8101754aBC46c52416550D1"),
                )
                balanceOf(alexWalletAddress).also(::println)
            }

            val callData3 =
                "0x38ed173900000000000000000000000000000000000000000000000000000000000da090000000000000000000000000000000000000000000000000000001b53ceba0ec00000000000000000000000000000000000000000000000000000000000000a0000000000000000000000000c43d2c472cf882e0b190063d66ee8ce78bf54da10000000000000000000000000000000000000000000000000000017d79f8aced00000000000000000000000000000000000000000000000000000000000000030000000000000000000000004dbcdf9b62e891a7cec5a2568c3f4faf9e8abe2b000000000000000000000000c778417e063141139fce010982780140aa0cd5ab0000000000000000000000001f9840a85d5af5bf1d1762f925bdaddc4201f984".let(
                    ::HexString
                )
            val contractAddress3 = ContractAddress("0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D")
            // sending uniswap transaction
            val txHash3 = nonceController.withNonce(alexWalletAddress) { nonce ->
                val signed = alexCredentials.signer.signContractTransaction(
                    nonce = nonce,
                    chainId = testETH.chainId,
                    to = contractAddress3,
                    contractData = callData3.prefixed,
                    value = 0.bi,
                    gasConfiguration = testETH.gasProvider.getGasConfiguration(
                        from = alexCredentials.address,
                        to = contractAddress3,
                        callData = callData3,
                        value = null,
                        executor = testBSC.executor
                    )
                )
                testBSC.executor.sendTransaction(signed)
            }

            testETH.executor.waitForTransactionReceipt(txHash3)
        }
    }

    //@Test
    fun testCrossChainRestrictions() {
        runBlocking {
            val (min, max) = testSdk.bscTestnetEthRinkebyClient.getAllowedRangeForInput(
                fromToken = testSdk.bscTestnet.token.BNB
            )

            println("Min allowed input: ${min.amount} BNB ($ ${testSdk.bscTestnetEthRinkebyClient.crossChain.minStableTokensAmountPerTrade})")
            println("Max allowed input: ${max.amount} BNB ($ ${testSdk.bscTestnetEthRinkebyClient.crossChain.maxStableTokensAmountPerTrade})")

            val amountIn = "50".bn

            println("Calculated meta swap trade BNB -> ETH for $amountIn BNB:")

            val trade = testSdk.bscTestnetEthRinkebyClient.findBestTradeExactIn(
                to = alexWalletAddress,
                fromToken = testSdk.bscTestnet.token.BNB,
                targetToken = testSdk.ethRinkeby.token.ETH,
                amountIn = testSdk.bscTestnet.token.BNB.convertRealToInteger(amountIn)
            )

            when (trade) {
                is CalculatedMetaSwapTrade.StableTokensGreaterThanMax ->
                    println("$trade")
                is CalculatedMetaSwapTrade.StableTokensLessThanMin ->
                    println("$trade")
                is CalculatedMetaSwapTrade.ExactIn ->
                    println("$trade")
                null ->
                    println("Cannot find a trade!")
            }
        }
    }
}
