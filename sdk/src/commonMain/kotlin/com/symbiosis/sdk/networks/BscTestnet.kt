@file:Suppress("PropertyName", "MemberVisibilityCanBePrivate")

package com.symbiosis.sdk.networks

import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.DecimalsNativeToken
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class BscTestnet(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(endpointUrl))

    override val networkName: String = "BscTestnet"

    override val chainIdInt = 0x61
    override val synthFabricAddressString = "0xC29cff9F2d649240a35F7401cb6B1C5B8227d676"
    override val portalAddressString = "0x7cD337E485a0ED3e2F508bfCbE3484A746552529"
    override val synthesizeAddressString = "0x68801662cab0D678E5216CB67DaD350271375024"
    override val bridgeAddressString = "0xBA7c80bb5d316c4eE55F96F47d1a1477fFD1aFb6"
    override val routerAddressString = "0xD99D1c33F9fC3444f8101754aBC46c52416550D1"
    override val metaRouterAddressString = "0x2901Cb45972516E1EBf7dcff43665b56d9171A46"
    override val metaRouterGatewayAddressString = "0x4b212745A02Dfe930942146A260E6B4C93C77ca7"

    val token = Tokens()
    override val tokens: List<DecimalsToken> = listOf(token.BNB, token.WBNB, token.BUSD, token.CAKE)

    override val nativeCurrency = token.BNB
    override val uniSwapBases: List<DecimalsErc20Token> = listOf(token.WBNB, token.CAKE, token.BUSD)
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