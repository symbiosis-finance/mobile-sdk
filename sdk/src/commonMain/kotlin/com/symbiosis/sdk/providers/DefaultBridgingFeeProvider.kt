package com.symbiosis.sdk.providers

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.BridgingFeeProvider
import com.symbiosis.sdk.serializer.BigIntSerializer
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.hex.HexString
import io.ktor.client.HttpClient
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object DefaultBridgingFeeProvider : BridgingFeeProvider {
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
        install(JsonFeature) {
            serializer = KotlinxSerializer(json)
        }
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

    override suspend fun getBridgingFee(
        chainFromId: BigInt,
        chainToId: BigInt,
        receiveSide: ContractAddress,
        callData: HexString
    ): BigInt = try {
        client
            .post<GetBridgingFeeResult>(urlString = "https://api.dev.symbiosis.finance/calculations/v1/swap/price") {
                body = GetBridgingFeeBody(
                    chainIdFrom = chainFromId,
                    chainIdTo = chainToId,
                    receiveSide = receiveSide.prefixed,
                    callData = callData
                )
            }.price
    } catch (t: Throwable) {
        println("Error while accessing default Symbiosis provider, using 0 bridging fee.")
        println(t.stackTraceToString())
        0.bi
    }
}
