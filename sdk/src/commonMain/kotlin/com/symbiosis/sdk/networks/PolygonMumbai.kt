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

class PolygonMumbai(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(CHAIN_ID, endpointUrl))

    override val networkName: String = "PolygonMumbai"

    override val synthFabricAddressString = "0x014aA1eD19b4B94430Ba664B44C130A7084b5bFa"
    override val portalAddressString = "0x24017eEB483fD95339B66d8e4be74eFFeE67E2cB"
    override val synthesizeAddressString = "0x12723824603df3cF4F55feB298aD0D00E1BFEf48"
    override val bridgeAddressString = "0x823389FfdF5F1BAD10eB52089E7195772A54ccBa"
    override val routerAddressString = "0xca33f6D096BDD7FcB28d708f631cD76E73Ecfc2d"
    override val metaRouterAddressString = "0x7591e9807f2557835EC1c0A4188800A4550d23cB"
    override val metaRouterGatewayAddressString = "0x996662AF968E3d798c879129Ec941da3a1CFe3e1"

    val token = Tokens()
    override val tokens: List<DecimalsToken> = listOf(token.MATIC, token.WMATIC, token.USDT)
    
    override val nativeCurrency: DecimalsNativeToken = token.MATIC
    override val uniSwapBases: List<DecimalsErc20Token> = listOf(token.WMATIC)
    override val dexEndpoints: List<DexEndpoint> = listOf(
        DexEndpoint.hardcoded(
            factoryContractAddress = "0x8a628F00710993c1cebbaa02338d2264ee7056C6",
            initCodeHash = "0x85f8ad645fe62917d6939782650649d3d7c4b5f25d81415a9fac4a9f341793ca",
            liquidityProviderFee = 0.003
        )
    )

    inner class Tokens internal constructor() {
        val USDT = DecimalsErc20Token(
            network = this@PolygonMumbai,
            tokenAddress = "0x9a01bf917477dd9f5d715d188618fc8b7350cd22",
            decimals = 6
        )
        val WMATIC = DecimalsErc20Token(
            network = this@PolygonMumbai,
            tokenAddress = "0x9c3C9283D3e44854697Cd22D3Faa240Cfb032889",
            decimals = 18
        )
        val MATIC = DecimalsNativeToken(WMATIC)

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)
    }
    
    companion object {
        val CHAIN_ID = 0x13881.bi
    }
}
