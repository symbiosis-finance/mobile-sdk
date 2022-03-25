@file:UseSerializers(BigIntSerializer.LongPrimitive::class)

package com.symbiosis.sdk.configuration

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.serializer.BigIntSerializer
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.hex.HexString
import kotlinx.serialization.UseSerializers

fun interface BridgingFeeProvider {
    suspend fun getBridgingFee(
        chainFromId: BigInt,
        chainToId: BigInt,
        receiveSide: ContractAddress,
        callData: HexString
    ): BigInt
}
