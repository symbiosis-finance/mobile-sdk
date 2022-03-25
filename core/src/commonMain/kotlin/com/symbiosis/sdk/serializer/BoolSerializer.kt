package com.symbiosis.sdk.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Boolean::class)
object BoolSerializer {
    override val descriptor = PrimitiveSerialDescriptor(
        serialName = "dev.icerock.web3.serializer.BoolSerializer",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: Boolean) {
        val string16 = value.toString()
        encoder.encodeString("0x$string16")
    }

    override fun deserialize(decoder: Decoder): Boolean {
        val hexString = decoder.decodeString()
        return hexString.last().digitToInt() == 1
    }
}
