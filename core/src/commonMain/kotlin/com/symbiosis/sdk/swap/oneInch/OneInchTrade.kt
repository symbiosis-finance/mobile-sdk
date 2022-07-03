package com.symbiosis.sdk.swap.oneInch

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.network.contract.checkTokenAllowance
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.transaction.Web3SwapTransaction
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.entity.WalletAddress
import dev.icerock.moko.web3.gas.GasConfiguration
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.nonce.NonceManager
import dev.icerock.moko.web3.requests.send
import dev.icerock.moko.web3.signing.Credentials

data class OneInchTrade(
    private val client: NetworkClient,
    private val oneInchClient: OneInchHttpClient,
    val recipient: EthereumAddress,
    val tokens: OneInchTokenPair,
    val amountIn: TokenAmount,
    val amountOutEstimated: TokenAmount,
    val callData: HexString,
    val to: ContractAddress,
    val value: BigInt,
    val gasPrice: BigInt,
    val gasLimit: BigInt,
    val slippageTolerance: Percentage,
    val fromAddress: EthereumAddress,
    val priceImpact: Percentage
) {

    private val signature = callData.withoutPrefix.take(n = 8)

    // https://github.com/symbiosis-finance/js-sdk/blob/f1d6b1df614fcc801d5cda967dbf00e4e0d3cf57/src/crosschain/oneInchTrade.ts#L120
    val callDataOffset = when (signature) {
        "b0431182", "d0a3b665" -> 100.bi
        "7c025200"             -> 260.bi
        "e449022e"             ->  36.bi
        "2e95b6c8", "bc80f1a8" ->  68.bi
        "9994dd15"             -> 132.bi
        "baba5255"             -> 292.bi
        else -> error("This one signature ($signature) for 1inch is unknown for us at the moment")
    }

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
            .checkTokenAllowance(walletAddress, approveSpender(), amountIn.raw)
    }

    suspend fun approveMaxIfRequired(credentials: Credentials) {
        if (!isApproveRequired(credentials.address))
            return

        client.getTokenContract(tokens.first.address)
            .approveMax(
                credentials = credentials,
                spender = approveSpender()
            )
    }

    suspend fun execute(credentials: Credentials): Web3SwapTransaction {
        // if you want custom gasProvider here
        // just call this function by yourself, so
        // next call will just check if approval required
        approveMaxIfRequired(credentials)

        val gasConfiguration = GasConfiguration.Legacy(gasPrice, gasLimit)

        val signed = client.nonceManager.withNonce(credentials.address) { nonce ->
            val result = when (credentials) {
                is Credentials.Local -> credentials.signContractTransaction(
                    nonce = nonce,
                    chainId = client.network.chainId,
                    to = to,
                    callData = callData,
                    value = value,
                    gasConfiguration = gasConfiguration
                )
                is Credentials.Async -> credentials.signContractTransaction(
                    nonce = nonce,
                    chainId = client.network.chainId,
                    to = to,
                    callData = callData,
                    value = value,
                    gasConfiguration = gasConfiguration
                )
            }

            return@withNonce NonceManager.NonceResult.Increase(result)
        }

        val hash = client.send(signed)

        return Web3SwapTransaction(client, hash)
    }
}
