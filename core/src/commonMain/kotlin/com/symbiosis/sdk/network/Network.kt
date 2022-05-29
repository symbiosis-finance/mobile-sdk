package com.symbiosis.sdk.network

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.configuration.SwapTTLProvider
import com.symbiosis.sdk.currency.DecimalsNativeToken
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.internal.nonce.NonceController
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.Web3Executor

interface Network {
    // General
    val chainId: BigInt

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
    val swapBases: List<Erc20Token>
    val dexEndpoints: List<DexEndpoint>

    val gasProvider: GasProvider
    val swapTTLProvider: SwapTTLProvider

    val executor: Web3Executor
    val nonceController: NonceController
}

fun Network.erc20TokenOf(contractAddress: ContractAddress) =
    Erc20Token(network = this, contractAddress)
