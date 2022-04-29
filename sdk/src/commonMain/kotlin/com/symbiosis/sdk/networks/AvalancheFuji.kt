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
    override val synthFabricAddressString = "0x24017eEB483fD95339B66d8e4be74eFFeE67E2cB"
    override val portalAddressString = "0x823389FfdF5F1BAD10eB52089E7195772A54ccBa"
    override val synthesizeAddressString = "0x58c96809ccE40c3849C7CC86fc79F81C9fcA78b6"
    override val bridgeAddressString = "0xc9Fd2AF244FEfb31A62A5A33B9D6261Cec2cb7aA"
    override val routerAddressString = "0x4F86a87985a2eD1E843c0b93755Ac06A3DbCc55E"
    override val metaRouterAddressString = "0x9666642b7B68281F912A0b4bee1d00b15ce7B28a"
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
