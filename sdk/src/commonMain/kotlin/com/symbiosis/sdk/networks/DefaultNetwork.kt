package com.symbiosis.sdk.networks

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.SwapTTLProvider
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.providers.DefaultTTLProvider
import dev.icerock.moko.web3.entity.ContractAddress

abstract class DefaultNetwork : Network {

    abstract val tokens: List<DecimalsToken>

    open val maxBlocksPerRequestInt: Int = 5_000
    override val maxBlocksPerRequest: BigInt get() = maxBlocksPerRequestInt.bi

    override val swapTTLProvider: SwapTTLProvider = DefaultTTLProvider

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

    override fun toString(): String = "$networkName (Predefined)"
}
