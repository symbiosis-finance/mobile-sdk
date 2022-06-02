@file:Suppress("MemberVisibilityCanBePrivate", "PropertyName")

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

open class AvalancheFuji(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String = "https://api.avax-test.network/ext/bc/C/rpc") : this(Web3(endpointUrl))

    override val networkName: String = "AvalancheFuji"

    override val chainIdInt = 0xA869
    override val synthFabricAddressString = "0x69fA0Ae9E3065B9d6c4c1909E101807bAaf3227e"
    override val portalAddressString = "0x14be03e34B05a87A028acfF0292C1AF135D26699"
    override val synthesizeAddressString = "0x9A857D526A9e53697a9Df5fFc40bCCD70E7A0388"
    override val bridgeAddressString = "0x68d12DD9cd42BD62A6F707A96B3dc8D1A6a9f076"
    override val routerAddressString = "0x4F86a87985a2eD1E843c0b93755Ac06A3DbCc55E"
    override val metaRouterAddressString = "0x1a82Ee67402bfE4225a2420A51De1276C0f1C614"
    override val metaRouterGatewayAddressString = "0xef4694BD32884A565D50c476B0D78F06dEBcFE27"

    val token = Tokens()
    override val tokens: List<Token> = listOf(token.AWAX, token.WAWAX, token.USDT)

    override val nativeCurrency: DecimalsNativeToken = token.AWAX
    override val swapBases: List<Erc20Token> = listOf(token.WAWAX, token.USDT)
    override val dexEndpoints: List<DexEndpoint> = listOf(
        DexEndpoint.hardcoded(
            factoryContractAddress = "0xb278D63e2E2a4aeb5A398eB87a91FF909B72C8D5",
            initCodeHash = "0x85f8ad645fe62917d6939782650649d3d7c4b5f25d81415a9fac4a9f341793ca",
            liquidityProviderFee = 0.003
        )
    )

    inner class Tokens internal constructor() {
        val USDT = DecimalsErc20Token(
            network = this@AvalancheFuji,
            tokenAddress = "0x9a01bf917477dd9f5d715d188618fc8b7350cd22",
            decimals = 6
        )
        val WAWAX = DecimalsErc20Token(
            network = this@AvalancheFuji,
            tokenAddress = "0xd00ae08403B9bbb9124bB305C09058E32C39A48c",
            decimals = 18
        )
        val AWAX = DecimalsNativeToken(WAWAX)

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)
    }
}
