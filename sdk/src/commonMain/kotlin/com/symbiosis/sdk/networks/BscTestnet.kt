@file:Suppress("PropertyName", "MemberVisibilityCanBePrivate")

package com.symbiosis.sdk.networks

import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.DecimalsNativeToken
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BscTestnet(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(endpointUrl))

    override val networkName: String = "BscTestnet"

    override val chainIdInt = 0x61
    override val synthFabricAddressString = "0xdBfb647247E4e402437f717FB154a990a6f5372d"
    override val portalAddressString = "0x1EE47a7DF64a8A23FA47458d9d7b148559b728ac"
    override val synthesizeAddressString = "0xF0f4F93CE9A0Ee6e9ad9406e0fea81843164fD74"
    override val bridgeAddressString = "0x67e2696fb65641902AA26DC9bABf76cE134CA377"
    override val routerAddressString = "0xD99D1c33F9fC3444f8101754aBC46c52416550D1"
    override val metaRouterAddressString = "0x8eF37F8FF0E4916017697A525d34f481CAa15090"
    override val metaRouterGatewayAddressString = "0xE9EF3E03cd8E4641B867F8A58d4311da8dAd49c8"

    val token = Tokens()
    override val tokens: List<Token> = listOf(token.BNB, token.WBNB, token.BUSD, token.CAKE)

    override val nativeCurrency = token.BNB
    override val swapBases: List<Erc20Token> = listOf(token.WBNB, token.CAKE, token.BUSD)
    override val dexEndpoints: List<DexEndpoint> = listOf(
        // Pancake
        DexEndpoint.hardcoded(
            factoryContractAddress = "0x6725F303b657a9451d8BA641348b6761A6CC7a17",
            initCodeHash = "0xd0d4c4cd0848c93cb4fd1f498d7013ee6bfb25783ea21593d5834f5d250ece66",
            liquidityProviderFee = 0.002
        )
    )

    inner class Tokens internal constructor() {
        val CAKE = DecimalsErc20Token(
            network = this@BscTestnet,
            tokenAddress = "0x41b5984f45afb2560a0ed72bb69a98e8b32b3cca",
            decimals = 18
        )
        val BUSD = DecimalsErc20Token(
            network = this@BscTestnet,
            tokenAddress = "0x9a01bf917477dd9f5d715d188618fc8b7350cd22",
            decimals = 18
        )
        val WBNB = DecimalsErc20Token(
            network = this@BscTestnet,
            tokenAddress = "0xae13d989dac2f0debff460ac112a837c89baa7cd",
            decimals = 18
        )
        val BNB = DecimalsNativeToken(WBNB)

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)
    }
}