package com.symbiosis.sdk.swap.oneInch

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.serializer.BigIntSerializer
import com.symbiosis.sdk.swap.Percentage
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.hex.HexString
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun OneInchHttpClient(network: Network): OneInchHttpClient =
    OneInchHttpClient(baseUrl = "https://api.1inch.io/v4.0/${network.chainId}")

class OneInchHttpClient(private val baseUrl: io.ktor.http.Url) {

    constructor(baseUrl: String) : this(URLBuilder(baseUrl).build())

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            val json = Json {
                ignoreUnknownKeys = true
            }
            json(json)
        }
        expectSuccess = true
        install(Logging) {
            level = LogLevel.ALL
        }
    }

    @Serializable
    private data class ApproveSpenderResponse(
        val address: ContractAddress
    )

    suspend fun approveSpender(): ContractAddress {
        val response = httpClient.get(baseUrl) {
            url.appendPathSegments("approve", "spender")
        }.body<ApproveSpenderResponse>()

        return response.address
    }

    @Serializable
    data class SwapResponse(
        val fromToken: TokenResponse,
        val toToken: TokenResponse,
        @Serializable(with = BigIntSerializer.StringPrimitive::class)
        val fromTokenAmount: BigInt,
        @Serializable(with = BigIntSerializer.StringPrimitive::class)
        val toTokenAmount: BigInt,
        val tx: TransactionResponse
    )

    @Serializable
    data class TransactionResponse(
        val from: EthereumAddress,
        val to: ContractAddress,
        val data: HexString,
        @Serializable(with = BigIntSerializer.StringPrimitive::class)
        val value: BigInt,
        @Serializable(with = BigIntSerializer.StringPrimitive::class)
        val gasPrice: BigInt,
        @Serializable(with = BigIntSerializer.LongPrimitive::class)
        val gas: BigInt
    )

    @Serializable
    data class ErrorResponse(
        val statusCode: Int,
        val error: String,
        val description: String? = null,
        val requestId: String? = null
    )

    sealed interface SwapResult {
        class Success(val swapResponse: SwapResponse) : SwapResult
        object InsufficientLiquidity : SwapResult
        object NotEnoughEthForGas : SwapResult
    }

    suspend fun swap(
        fromTokenAddress: ContractAddress,
        toTokenAddress: ContractAddress,
        amount: BigInt,
        fromAddress: EthereumAddress,
        slippageTolerance: Percentage,
        destReceiver: EthereumAddress = fromAddress
    ): SwapResult {
        require(slippageTolerance > 0 && slippageTolerance < 50)

        try {
            val response = httpClient.get(baseUrl) {
                url.appendPathSegments("swap")

                parameter("fromTokenAddress", fromTokenAddress.prefixed)
                parameter("toTokenAddress", toTokenAddress.prefixed)
                parameter("amount", "$amount")
                parameter("fromAddress", fromAddress.prefixed)
                parameter("slippage", "${slippageTolerance.intPercentage}")
                parameter("disableEstimate", "true")
                parameter("allowPartialFill", "false")
                parameter("usePatching", "true")
                parameter("destReceiver", destReceiver.prefixed)
            }.body<SwapResponse>()

            return SwapResult.Success(response)
        } catch (exception: ClientRequestException) {
            val response = exception.response.body<ErrorResponse>()

            return when (response.error) {
                "Insufficient liquidity" -> SwapResult.InsufficientLiquidity
                "Cannot estimate" -> SwapResult.InsufficientLiquidity
                "You may not have enough ETH balance for gas fee" -> SwapResult.NotEnoughEthForGas
                "FromTokenAddress cannot be equals to toTokenAddress" ->
                    error("FromTokenAddress cannot be equals to toTokenAddress")
                "Cannot estimate. Don't forget about miner fee. Try to leave the buffer of ETH for gas" ->
                    SwapResult.NotEnoughEthForGas
                // you should check by yourself that amountIn is
                // greater than your balance
                "Not enough balance" -> error("Not enough balance")
                "Not enough allowance" -> error("Not enough allowance")
                // when trade not found 400 this occurs
                else -> return SwapResult.InsufficientLiquidity
            }
        }
    }

    @Serializable
    data class TokenResponse(
        val symbol: String,
        val name: String,
        @Serializable(with = BigIntSerializer.LongPrimitive::class)
        val decimals: BigInt,
        val logoUrl: String? = null,
        val address: ContractAddress
    )
}


