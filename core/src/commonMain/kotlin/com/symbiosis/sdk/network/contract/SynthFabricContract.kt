package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.network.contract.abi.fabricContractAbi
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.ABIDecoder
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.requests.executeBatch


@Suppress("MemberVisibilityCanBePrivate")
class SynthFabricContract internal constructor(
    val network: Network,
    private val executor: Web3Executor,
    private val wrapped: SmartContract
) {
    val address = wrapped.contractAddress

    fun getSyntheticTokenRequest(
        address: ContractAddress,
        chainId: BigInt
    ) = wrapped.readRequest(
        method = "getSyntRepresentation",
        params = listOf(address, chainId)
    ) { synths ->
        if (synths.isEmpty())
            return@readRequest null
        return@readRequest Erc20Token(
            network = network,
            tokenAddress = (synths.first() as EthereumAddress).run { ContractAddress(prefixed) }
        )
    }

    suspend fun getSyntheticToken(
        address: ContractAddress,
        chainId: BigInt
    ): Erc20Token? =
        executor.executeBatch(getSyntheticTokenRequest(address, chainId)).first()

    fun getRealTokenAddressRequest(
        synthAddress: ContractAddress
    ) = wrapped.readRequest(
        method = "getRealRepresentation",
        params = listOf(synthAddress),
    ) { reals  ->
        if (reals.isEmpty())
            return@readRequest null

        (reals.first() as EthereumAddress).run { ContractAddress(prefixed) }
    }

    suspend fun getRealTokenAddress(
        synthAddress: ContractAddress
    ): ContractAddress? = executor.executeBatch(getRealTokenAddressRequest(synthAddress)).first()
}

suspend fun SynthFabricContract.getSyntheticToken(currency: Erc20Token) =
    getSyntheticToken(currency.tokenAddress, currency.network.chainId)

suspend fun SynthFabricContract.getRealTokenAddress(currency: Erc20Token) =
    getRealTokenAddress(currency.tokenAddress)

data class ApprovedTokenFromContract(
    val realTokenAddress: ContractAddress,
    val realTokenChainId: BigInt,
    val synthTokenAddress: ContractAddress
)

fun LogEvent.parseApprovedToken(): ApprovedTokenFromContract = ABIDecoder.decodeCallDataForInputs(
    abis = fabricContractAbi,
    name = "RepresentationCreated",
    callData = data
).let { (realTokenAddress, realTokenChainId, synthTokenAddress) ->
    ApprovedTokenFromContract(
        realTokenAddress = ContractAddress((realTokenAddress as EthereumAddress).prefixed),
        realTokenChainId = (realTokenChainId as BigInt),
        synthTokenAddress = ContractAddress((synthTokenAddress as EthereumAddress).prefixed)
    )
}