package com.symbiosis.sdk

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.thisOrWrapped
import com.symbiosis.sdk.network.contract.getRealTokenAddress
import com.symbiosis.sdk.network.contract.getSyntheticToken
import com.symbiosis.sdk.network.getTokenContract
import com.symbiosis.sdk.network.networkClient
import dev.icerock.moko.web3.BlockState
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.crypto.KeccakParameter
import dev.icerock.moko.web3.crypto.digestKeccak
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.requests.getNativeBalance
import dev.icerock.moko.web3.requests.polling.newLogsShortPolling
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class SymbiosisSdkTestnetsTest {

    //    @Test
    fun getEthereumBalance() {
        runBlocking {
            println(testETH.networkClient.getNativeBalance(denWalletAddress))
        }
    }

    //    @Test
    fun getBscBalance() {
        runBlocking {
            println(testBSC.networkClient.getNativeBalance(denWalletAddress))
        }
    }

    /**
     * [Any network] -> [BSC]
     */
//    @Test
    fun synthAndRealPairResolving() {
        runBlocking {
            val sourceToken = testETH.tokens
                .filterIsInstance<DecimalsErc20Token>()
                .random()
                .thisOrWrapped

            testBSC.networkClient.also { client ->
                val synthRepresentation = client.synthFabric
                    .getSyntheticToken(sourceToken)
                    ?.also { println("Synthetic representation found: ${it.tokenAddress}") }
                    ?: run {
                        println("Synth representation on BSC not found for $sourceToken currency, running test again")
                        return@runBlocking synthAndRealPairResolving()
                    }

                val realRepresentation = client.synthFabric
                    .getRealTokenAddress(synthRepresentation)
                    .also { println("Real representation found: $it") }

                assertEquals(
                    sourceToken.tokenAddress,
                    realRepresentation,
                    message = "Synthetic and real tokens are different"
                )
            }
        }
    }

    /**
     * Получить настоящий токен CAKE в сети BSC из синтетического токена sCAKE в сети Rinkeby
     */
//    @Test
    fun getCAKERealRepresentationInBSC() {
        // https://rinkeby.etherscan.io/address/0xc3DB2b1531b07d75DA09Ca408780639Cc05B0640
        val bscSynthCakeTokenAddress = ContractAddress("0xc3DB2b1531b07d75DA09Ca408780639Cc05B0640")
        runBlocking {
            val result = testBSC.networkClient.synthFabric.getRealTokenAddress(
                synthAddress = bscSynthCakeTokenAddress,
            )
            println(result)
        }
    }

    //    @Test
    fun synthAlp888() = runBlocking {
        val currency = testETH.token.UNI
        val networkClient = testETH.networkClient
        val synthNetworkClient = testBSC.networkClient

        val synthCurrency = synthNetworkClient.synthFabric.getSyntheticToken(currency)?.also { token ->
            println("Synthetic token found ${token.tokenAddress.prefixed}")
        } ?: error("Synth currency for $currency not found")

        val synthAmount = BigInt("10000000000000000000")

        val token = networkClient.getTokenContract(currency)
        val synthToken = synthNetworkClient.getTokenContract(synthCurrency)

//        val ws = networkClient.connectWebsocket(coroutineScope = this)
//        val synthWs = synthNetworkClient.connectWebsocket(coroutineScope = this)

        val balanceBefore = token.balanceOf(alexWalletAddress)
        val synthBalanceBefore = synthToken.balanceOf(alexWalletAddress)

        val targetBalance = balanceBefore - synthAmount
        val synthTargetBalance = synthBalanceBefore + synthAmount

        val hash = networkClient.portal.synthesize(
            credentials = alexCredentials,
            amount = synthAmount,
            realCurrencyAddress = currency.tokenAddress,
            targetNetwork = testBSC,
            stableBridgingFee = 0.bi
        )
        println("Transaction sent ${hash.prefixed}")

//        ws.subscribeWebSocketWithFilter(SubscriptionParam.NewHeads).first {
//            token.balanceOf(alexWallet.address) <= targetBalance
//        }
        println("Eth block mined")

//        synthWs.subscribeWebSocketWithFilter(SubscriptionParam.NewHeads).first {
//            synthToken.balanceOf(alexWallet.address) >= synthTargetBalance
//        }
        println("Bsc block mined")

        val balanceAfter = token.balanceOf(alexWalletAddress)
        val synthBalanceAfter = synthToken.balanceOf(alexWalletAddress)

        println("Balance before: $balanceBefore")
        println("Balance after: $balanceAfter")
        println("Synth balance before: $synthBalanceBefore")
        println("Synth balance after: $synthBalanceAfter")

        return@runBlocking
    }

    //    @Test
    fun unSynthAlp888() = runBlocking {
        val synthAmount = 100_000_000_000_000.bi
        val currency = testETH.token.UNI
        testBSC.networkClient.also { client ->
            val synthCurrency = testBSC.networkClient.synthFabric.getSyntheticToken(currency)
                ?: error("Synth currency for $currency not found")
            val synthToken = client.getTokenContract(synthCurrency)

            println(synthToken.balanceOf(alexWalletAddress))
//        println(symbiosisEth.getRealRepresentationInChain(synthCurrency.tokenAddress))
            val hash = client.synthesize.burnSynthTokens(
                credentials = alexCredentials,
                amount = synthAmount,
                synthCurrencyAddress = synthCurrency.tokenAddress,
                targetNetwork = testETH,
                stableBridgingFee = 0.bi
            )
            println("Transaction sent ${hash.prefixed}")

            while (true)
                println(synthToken.balanceOf(alexWalletAddress))

            return@runBlocking
        }
    }

    //    @Test
    fun wrappedTest() = runBlocking {
        testETH.networkClient
            .getWrappedTokenContract(ContractAddress("0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"))
            .wrap(amount = 1_000_000.bi, credentials = alexCredentials)
        return@runBlocking
    }

    //@Test
    fun eventsTest() = runBlocking {
        testETH.networkClient.also { client ->
            client.newLogsShortPolling(
                address = client.synthFabric.address.also(::println),
                topics = listOf(
                    "RepresentationCreated(address,uint256,address)"
                        .digestKeccak(KeccakParameter.KECCAK_256)
                        .let(::Hex32String)
                ),
                fromBlock = BlockState.Earliest
            ).collect {
                println(it)
            }

            return@runBlocking
        }
    }
}
