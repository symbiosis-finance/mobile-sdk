package com.symbiosis.sdk.swap.oneInch

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.gas.GasConfiguration
import com.symbiosis.sdk.internal.kbignum.UINT256_MAX
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.network.contract.checkTokenAllowance
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.hex.HexString

data class OneInchTrade(
    private val client: NetworkClient,
    private val oneInchClient: OneInchHttpClient,
    val recipient: EthereumAddress,
    val tokens: OneInchTokenPair,
    val amountIn: BigInt,
    val amountOutEstimated: BigInt,
    val callData: HexString,
    val to: ContractAddress,
    val value: BigInt,
    val gasPrice: BigInt,
    val gasLimit: BigInt,
    val slippageTolerance: Percentage,
    val fromAddress: EthereumAddress
) {
    private var spenderCache: ContractAddress? = null
    suspend fun approveSpender(): ContractAddress {
        if (spenderCache == null)
            spenderCache = oneInchClient.approveSpender()  // synchronization is not bottleneck

        return spenderCache!! // always not null
    }

    suspend fun isApproveRequired(walletAddress: WalletAddress): Boolean {
        val inputToken = tokens.first

        if (inputToken == OneInchToken.Native)
            return false

        return !client
            .getTokenContract(inputToken.address)
            .checkTokenAllowance(walletAddress, approveSpender(), BigInt.UINT256_MAX)
    }

    suspend fun approveMaxIfRequired(
        credentials: Credentials,
        gasProvider: GasProvider? = null
    ) {
        if (!isApproveRequired(credentials.address))
            return

        client.getTokenContract(tokens.first.address)
            .approveMax(
                credentials = credentials,
                spender = approveSpender(),
                gasProvider = gasProvider
            )
    }

    suspend fun execute(credentials: Credentials) {
        // if you want custom gasProvider here
        // just call this function by yourself, so
        // next call will just check if approval required
        approveMaxIfRequired(credentials)

        client.network.nonceController.withNonce(credentials.address) { nonce ->
            credentials.signer.signContractTransaction(
                nonce = nonce,
                chainId = client.network.chainId,
                to = to,
                contractData = callData.prefixed,
                value = value,
                gasConfiguration = GasConfiguration.Legacy(gasPrice, gasLimit)
            )
        }
    }
}
