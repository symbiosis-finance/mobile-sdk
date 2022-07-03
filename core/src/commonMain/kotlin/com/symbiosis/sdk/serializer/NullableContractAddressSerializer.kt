package com.symbiosis.sdk.serializer

import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.entity.ContractAddress
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object NullableContractAddressSerializer : KSerializer<ContractAddress?> {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "nullableContractAddress",
        kind = PrimitiveKind.STRING
    )

    override fun deserialize(decoder: Decoder): ContractAddress? {
        val bi = decoder.decodeSerializableValue(BigIntSerializer.StringPrimitive)
        return ContractAddress(value = "0x" + (bi.takeIf { it > 0.bi }?.toString(RADIX) ?: return null))
    }

    override fun serialize(encoder: Encoder, value: ContractAddress?) {
        val bi = value?.bigInt ?: 0.bi
        encoder.encodeSerializableValue(BigIntSerializer.StringPrimitive, value = bi)
    }

    private const val RADIX = 16
}
