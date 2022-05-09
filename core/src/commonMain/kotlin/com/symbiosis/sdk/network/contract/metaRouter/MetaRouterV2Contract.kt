package com.symbiosis.sdk.network.contract.metaRouter

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.contract.write
import com.symbiosis.sdk.internal.nonce.NonceController
import com.symbiosis.sdk.network.contract.TokenContract
import com.symbiosis.sdk.network.contract.approveMaxIfNeed
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.hex.HexString

/**
 * contract to work with off chain meta router
 */
class MetaRouterV2Contract(
    private val metaRouterV2SmartContract: SmartContract,
    private val metaRouterGatewayAddress: ContractAddress,
    private val nonceController: NonceController,
    private val defaultGasProvider: GasProvider,
    private val tokenContractProvider: (ContractAddress) -> TokenContract
) {
    sealed interface FirstToken {
        object Native : FirstToken
        class Erc20(val address: ContractAddress) : FirstToken
    }
    /**
     * meta route contract method
     * @param chainId outbound (or start) network
     * @param credentials user's wallet
     * @param firstSwapCallData first on-chain swap call data
     * @param secondSwapCallData nerve pool swap call data (empty by default, must be empty if route is reversed)
     * @param approvedTokens list of tokens to approve for meta router (first in trade and stable pool tokens)
     * @param firstDexRouter decentralized exchange on first network
     * @param secondDexRouter decentralized exchange on nerve pool
     * @param amount value of token to spend
     * @param relayRecipient portal or synthesize contract address (depends on strict or reversed mode)
     * @param otherSideCallData call data to execute on inbound (final) network
     */
    suspend fun metaRouteV2(
        firstToken: FirstToken,
        chainId: BigInt,
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
        if (firstToken is FirstToken.Erc20) {
            tokenContractProvider(firstToken.address).approveMaxIfNeed(
                credentials = credentials,
                spender = metaRouterGatewayAddress,
                amount = amount,
                gasProvider = gasProvider
            )
        }

        return metaRouterV2SmartContract.write(
            chainId = chainId,
            nonceController = nonceController,
            credentials = credentials,
            method = "metaRouteV2",
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
            value = if (firstToken is FirstToken.Native) amount else 0.bi,
            gasProvider = gasProvider ?: defaultGasProvider
        )
    }
}