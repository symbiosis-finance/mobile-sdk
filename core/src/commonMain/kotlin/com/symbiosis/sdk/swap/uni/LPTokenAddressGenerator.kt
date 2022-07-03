package com.symbiosis.sdk.swap.uni

import com.symbiosis.sdk.contract.sortedAddresses
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.thisOrWrapped
import com.symbiosis.sdk.dex.DexEndpoint
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.crypto.Keccak
import dev.icerock.moko.web3.crypto.KeccakParameter
import dev.icerock.moko.web3.crypto.digestKeccak
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.HexString

object LPTokenAddressGenerator {
    fun generate(
        factory: ContractAddress,
        initCodeHash: Hex32String,
        pair: NetworkTokenPair
    ): ContractAddress {
        val (token1, token2) = listOf(
            pair.first.thisOrWrapped.tokenAddress,
            pair.second.thisOrWrapped.tokenAddress
        ).sortedAddresses()

        val salt = Keccak.digest(
            value = token1.byteArray + token2.byteArray,
            parameter = KeccakParameter.KECCAK_256
        )

        return getCreate2Address(
            factory = factory,
            salt = salt,
            initCodeHash = initCodeHash.byteArray
        )
    }

    private fun getCreate2Address(
        factory: ContractAddress,
        salt: ByteArray,
        initCodeHash: ByteArray
    ) = (HexString("0xff").byteArray + factory.byteArray + salt + initCodeHash)
        .digestKeccak(KeccakParameter.KECCAK_256)
        .drop(n = 12)
        .toByteArray()
        .let(::HexString)
        .withoutPrefix
        .let(::ContractAddress)
        .toChecksummedAddress()

    private const val RADIX = 16
}

fun LPTokenAddressGenerator.generate(dex: DexEndpoint, pair: NetworkTokenPair) =
    generate(
        factory = dex.factoryContractAddress,
        initCodeHash = dex.initCodeHash,
        pair = pair
    )
