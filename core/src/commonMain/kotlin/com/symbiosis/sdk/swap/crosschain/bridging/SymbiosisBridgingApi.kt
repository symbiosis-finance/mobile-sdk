package com.symbiosis.sdk.swap.crosschain.bridging

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.serializer.BigIntSerializer
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.hex.HexString
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object SymbiosisBridgingApi {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val client = HttpClient {
        install(Logging) {
            level = LogLevel.NONE
        }
        install(DefaultRequest) {
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            json(json)
        }
        expectSuccess = true
    }

    @Serializable
    private data class GetBridgingFeeBody(
        @Serializable(with = BigIntSerializer.LongPrimitive::class)
        @SerialName("chain_id_from")
        val chainIdFrom: BigInt,
        @SerialName("chain_id_to")
        @Serializable(with = BigIntSerializer.LongPrimitive::class)
        val chainIdTo: BigInt,
        @SerialName("receive_side")
        val receiveSide: String,
        @SerialName("call_data")
        val callData: HexString
    )

    @Serializable
    private data class GetBridgingFeeResult(
        @Serializable(with = BigIntSerializer.LongPrimitive::class)
        val price: BigInt
    )

    suspend fun getBridgingFee(
        chainFromId: BigInt,
        chainToId: BigInt,
        receiveSide: ContractAddress,
        callData: HexString
    ): BigInt = try {
        client
            .post(urlString = "https://api.dev.symbiosis.finance/calculations/v1/swap/price") {
                setBody(
                    body = GetBridgingFeeBody(
                        chainIdFrom = chainFromId,
                        chainIdTo = chainToId,
                        receiveSide = receiveSide.prefixed,
                        callData = callData
                    )
                )
            }.body<GetBridgingFeeResult>().price
    } catch (t: Throwable) {
        println("Error while accessing default Symbiosis provider, using 0 bridging fee.")
        println(t.stackTraceToString())
        0.bi
    }
}
