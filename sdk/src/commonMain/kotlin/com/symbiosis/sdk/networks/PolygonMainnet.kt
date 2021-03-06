package com.symbiosis.sdk.networks

import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.DecimalsNativeToken
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class PolygonMainnet(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(endpointUrl))

    override val networkName = "PolygonMainnet"

    override val chainIdInt = 0x89
    override val synthFabricAddressString = "0x947a0d452b40013190295a4151A090E1638Fb848"
    override val portalAddressString = "0xD7F9989bE0d15319d13d6FA5d468211C89F0b147"
    override val synthesizeAddressString = "0xb80fDAA74dDA763a8A158ba85798d373A5E84d84"
    override val bridgeAddressString = "0xd5F0f8dB993D26F5df89E70a83d32b369DcCdaa0"
    override val routerAddressString = "0xa5E0829CaCEd8fFDD4De3c43696c57F7D7A678ff"
    override val metaRouterAddressString = "0x733D33FA01424F83E9C095af3Ece80Ed6fa565F1"
    override val metaRouterGatewayAddressString = "0xF3273BD35e4Ad4fcd49DabDee33582b41Cbb9d77"

    val token = Tokens()
    override val tokens: List<DecimalsToken> = listOf(token.MATIC, token.WMATIC, token.USDC)

    override val nativeCurrency = token.MATIC
    override val uniSwapBases: List<DecimalsErc20Token> = listOf(token.WMATIC, token.USDC)
    override val dexEndpoints: List<DexEndpoint> = listOf(
        DexEndpoint.hardcoded(
            factoryContractAddress = "0x5757371414417b8C6CAad45bAeF941aBc7d3Ab32",
            initCodeHash = "0x96e8ac4277198ff8b6f785478aa9a39f403cb768dd02cbee326c3e7da348845f",
            liquidityProviderFee = 0.003
        )
    )

    inner class Tokens internal constructor() {
        val USDC = DecimalsErc20Token(
            network = this@PolygonMainnet,
            tokenAddress = "0x2791bca1f2de4661ed88a30c99a7a9449aa84174",
            decimals = 6
        )
        val BNB = DecimalsErc20Token(
            network = this@PolygonMainnet,
            tokenAddress = "0x3BA4c387f786bFEE076A58914F5Bd38d668B42c3",
            decimals = 18
        )
        val WMATIC = DecimalsErc20Token(
            network = this@PolygonMainnet,
            tokenAddress = "0x0d500b1d8e8ef31e21c99d1db9a6444d3adf1270",
            decimals = 18
        )
        val MATIC = DecimalsNativeToken(WMATIC)

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)

    }
}