package com.symbiosis.sdk.network

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.configuration.SwapTTLProvider
import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.DecimalsNativeToken
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.dex.DexEndpoint
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.entity.ContractAddress

interface Network {
    val maxBlocksPerRequest: BigInt

    // General
    val chainId: BigInt get() = executor.chainId

    /**
     * Used only for pretty print, for identifying the network use [chainId]
     */
    val networkName: String

    // Contracts
    val synthFabricAddress: ContractAddress
    val portalAddress: ContractAddress
    val synthesizeAddress: ContractAddress
    val bridgeAddress: ContractAddress
    val routerAddress: ContractAddress
    val metaRouterAddress: ContractAddress
    val metaRouterGatewayAddress: ContractAddress

    val nativeCurrency: DecimalsNativeToken
    val uniSwapBases: List<DecimalsErc20Token>
    val dexEndpoints: List<DexEndpoint>

    val swapTTLProvider: SwapTTLProvider

    val executor: Web3Executor
}

fun Network.erc20TokenOf(contractAddress: ContractAddress) =
    Erc20Token(network = this, contractAddress)
