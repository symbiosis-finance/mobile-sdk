/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.symbiosis.sdk.serializer

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.hex.HexString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object BigIntSerializer {
    object StringPrimitive : KSerializer<BigInt> {
        override val descriptor = PrimitiveSerialDescriptor(
            serialName = "dev.icerock.web3.serializer.BigIntSerializer.StringPrimitive",
            kind = PrimitiveKind.STRING
        )

        override fun serialize(encoder: Encoder, value: BigInt) =
            encoder.encodeString(HexString(value).prefixed)

        override fun deserialize(decoder: Decoder): BigInt =
            HexString(decoder.decodeString()).bigInt
    }
    object LongPrimitive : KSerializer<BigInt> {
        override val descriptor = PrimitiveSerialDescriptor(
            serialName = "dev.icerock.web3.serializer.BigIntSerializer.LongPrimitive",
            kind = PrimitiveKind.LONG
        )
        override fun serialize(encoder: Encoder, value: BigInt) =
            encoder.encodeLong(value.toString().toLong())

        override fun deserialize(decoder: Decoder): BigInt =
            decoder.decodeLong().toString().bi
    }

    private const val RADIX = 16
}
