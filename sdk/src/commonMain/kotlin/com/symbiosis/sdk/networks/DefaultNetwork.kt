package com.symbiosis.sdk.networks

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.BridgingFeeProvider
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.configuration.SwapTTLProvider
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.internal.nonce.NonceController
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.providers.DefaultBridgingFeeProvider
import com.symbiosis.sdk.providers.DefaultGasProvider
import com.symbiosis.sdk.providers.DefaultTTLProvider
import dev.icerock.moko.web3.ContractAddress

abstract class DefaultNetwork : Network {
    abstract val tokens: List<Token>

    override val gasProvider: GasProvider = DefaultGasProvider
    override val bridgingFeeProvider: BridgingFeeProvider = DefaultBridgingFeeProvider
    override val swapTTLProvider: SwapTTLProvider = DefaultTTLProvider

    abstract val chainIdInt: Int
    final override val chainId: BigInt get() = chainIdInt.bi

    abstract val synthFabricAddressString: String
    final override val synthFabricAddress: ContractAddress get() = ContractAddress(synthFabricAddressString)
    abstract val portalAddressString: String
    final override val portalAddress: ContractAddress get() = ContractAddress(portalAddressString)
    abstract val synthesizeAddressString: String
    final override val synthesizeAddress: ContractAddress get() = ContractAddress(synthesizeAddressString)
    abstract val bridgeAddressString: String
    final override val bridgeAddress: ContractAddress get() = ContractAddress(bridgeAddressString)
    abstract val routerAddressString: String
    final override val routerAddress: ContractAddress get() = ContractAddress(routerAddressString)
    abstract val metaRouterAddressString: String
    override val metaRouterAddress: ContractAddress get() = ContractAddress(metaRouterAddressString)
    abstract val metaRouterGatewayAddressString: String
    final override val metaRouterGatewayAddress: ContractAddress get() = ContractAddress(metaRouterGatewayAddressString)

    override val nonceController: NonceController by lazy {
        NonceController(executor)
    }

    override fun toString(): String = "$networkName (Predefined)"
}
