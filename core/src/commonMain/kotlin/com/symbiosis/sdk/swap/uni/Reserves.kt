package com.symbiosis.sdk.swap.uni

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.internal.kbignum.toBigNum
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder

sealed interface Reserves {
    class Web3Deserializer(private val isReversed: Boolean) : DeserializationStrategy<Reserves> {
        override val descriptor = PrimitiveSerialDescriptor(
            serialName = "web3Reserves",
            kind = PrimitiveKind.STRING
        )

        override fun deserialize(decoder: Decoder): Reserves {
            val string = decoder.decodeString().takeIf { it != "0x" } ?: return Empty

            val (reserve1, reserve2) = string
                .removePrefix(prefix = "0x")
                .chunked(size = WEB3_PARAM_SIZE * 2)
                .map { it.bi(radix = 16) }

            return if (isReversed) ReservesData(reserve2, reserve1) else ReservesData(reserve1, reserve2)
        }
    }

    object Empty : Reserves

    private companion object {
        private const val WEB3_PARAM_SIZE = 32
    }
}

data class ReservesData(
    val reserve1: BigInt,
    val reserve2: BigInt
) : Reserves {
    val price1 get() = reserve2.toBigNum().div(reserve1.toBigNum(), precision = 18)
    val price2 get() = reserve1.toBigNum().div(reserve2.toBigNum(), precision = 18)
}

