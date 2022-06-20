package com.symbiosis.sdk.network.contract.metaRouter

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.contract.write
import com.symbiosis.sdk.internal.nonce.NonceController
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.hex.HexString

/**
 * contract to work with off chain meta router
 */
class MetaRouterContract(
    private val metaRouterV2SmartContract: SmartContract,
    private val metaRouterGatewayAddress: ContractAddress,
    private val nonceController: NonceController,
    private val defaultGasProvider: GasProvider,
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
        otherSideCallData: HexString,
        gasProvider: GasProvider? = null
    ): TransactionHash {
        return metaRouterV2SmartContract.write(
            chainId = chainId,
            nonceController = nonceController,
            credentials = credentials,
            method = "metaRoute",
            params = listOf(
                listOf(
                    firstSwapCallData?.byteArray ?: byteArrayOf(),
                    secondSwapCallData?.byteArray ?: byteArrayOf(),
                    approvedTokens.map(ContractAddress::bigInt),
                    firstDexRouter.bigInt,
                    secondDexRouter.bigInt,
                    amount,
                    nativeIn,
                    relayRecipient.bigInt,
                    otherSideCallData.byteArray
                )
            ),
            value = if (nativeIn) amount else 0.bi,
            gasProvider = gasProvider ?: defaultGasProvider
        )
    }
}
