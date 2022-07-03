@file:Suppress("MemberVisibilityCanBePrivate", "PropertyName")

package com.symbiosis.sdk.networks

import com.soywiz.kbignum.bi
import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.DecimalsNativeToken
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.entity.ContractAddress

open class AvalancheFuji(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String = "https://api.avax-test.network/ext/bc/C/rpc") :
            this(Web3(CHAIN_ID, endpointUrl))

    override val maxBlocksPerRequestInt = 2048

    override val networkName: String = "AvalancheFuji"

    override val synthFabricAddressString = "0x24017eEB483fD95339B66d8e4be74eFFeE67E2cB"
    override val portalAddressString = "0x823389FfdF5F1BAD10eB52089E7195772A54ccBa"
    override val synthesizeAddressString = "0x58c96809ccE40c3849C7CC86fc79F81C9fcA78b6"
    override val bridgeAddressString = "0xc9Fd2AF244FEfb31A62A5A33B9D6261Cec2cb7aA"
    override val routerAddressString = "0x4F86a87985a2eD1E843c0b93755Ac06A3DbCc55E"
    override val metaRouterAddressString = "0x1A3fb518fBcf9f0010acbeFea2749E43A045C4F1"
    override val metaRouterGatewayAddressString = "0xbcF3931C3f84A55a2DF8979D168f9DC97Ce93ED6"

    val token = Tokens()
    override val tokens: List<DecimalsToken> = listOf(token.AVAX, token.WAVAX, token.USDT)

    override val nativeCurrency: DecimalsNativeToken = token.AVAX
    override val uniSwapBases: List<DecimalsErc20Token> = listOf(token.WAVAX, token.USDT)
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
        val WAVAX = DecimalsErc20Token(
            network = this@AvalancheFuji,
            tokenAddress = "0xd00ae08403B9bbb9124bB305C09058E32C39A48c",
            decimals = 18
        )
        val AVAX = DecimalsNativeToken(WAVAX)

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)
    }

    companion object {
        val CHAIN_ID = 0xA869.bi
    }
}
