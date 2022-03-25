package com.symbiosis.sdk.serializer

import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.network.Network
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class NullableTokenCryptoCurrencySerializer(private val network: Network) : KSerializer<Erc20Token?> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "nullableTokenCryptoCurrency",
        kind = PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder): Erc20Token? {
        val contractAddress = decoder.decodeSerializableValue(NullableContractAddressSerializer) ?: return null
        return Erc20Token(
            network = network,
            tokenAddress = contractAddress
        )
    }

    override fun serialize(encoder: Encoder, value: Erc20Token?) {
        encoder.encodeSerializableValue(NullableContractAddressSerializer, value = value?.tokenAddress)
    }

    companion object {
        private const val RADIX = 16
    }
}
