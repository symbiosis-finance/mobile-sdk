package com.symbiosis.sdk.network.contract.metaRouter

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.entity.TransactionHash
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.signing.Credentials

/**
 * contract to work with off chain meta router
 */
class MetaRouterContract(
    private val metaRouterV2SmartContract: SmartContract,
    private val metaRouterGatewayAddress: ContractAddress,
    private val chainId: BigInt
) {

    suspend fun metaRoute(
        credentials: Credentials,
        firstSwapCallData: HexString?,
        secondSwapCallData: HexString?,
        approvedTokens: List<ContractAddress>,
        firstDexRouter: ContractAddress,
        secondDexRouter: ContractAddress,
        amount: BigInt,
        nativeIn: Boolean,
        relayRecipient: ContractAddress,
        otherSideCallData: HexString
    ): TransactionHash {
        return metaRouterV2SmartContract.write(
            credentials = credentials,
            method = "metaRoute",
            params = listOf(
                listOf(
                    firstSwapCallData?.byteArray ?: byteArrayOf(),
                    secondSwapCallData?.byteArray ?: byteArrayOf(),
                    approvedTokens,
                    firstDexRouter,
                    secondDexRouter,
                    amount,
                    nativeIn,
                    relayRecipient,
                    otherSideCallData.byteArray
                )
            ),
            value = if (nativeIn) amount else 0.bi
        )
    }
}
