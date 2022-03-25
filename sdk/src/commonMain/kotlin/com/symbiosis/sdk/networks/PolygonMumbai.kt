package com.symbiosis.sdk.networks

import com.symbiosis.sdk.currency.DecimalsNativeToken
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class PolygonMumbai(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(endpointUrl))

    override val networkName: String = "PolygonMumbai"

    override val chainIdInt = 0x13881
    override val synthFabricAddressString = "0x014aA1eD19b4B94430Ba664B44C130A7084b5bFa"
    override val portalAddressString = "0x24017eEB483fD95339B66d8e4be74eFFeE67E2cB"
    override val synthesizeAddressString = "0x12723824603df3cF4F55feB298aD0D00E1BFEf48"
    override val bridgeAddressString = "0x823389FfdF5F1BAD10eB52089E7195772A54ccBa"
    override val routerAddressString = "0xca33f6D096BDD7FcB28d708f631cD76E73Ecfc2d"
    override val metaRouterAddressString = "0xd70077A7e64473F2B606fDeE43014d63d3F8dFB2"

    val token = Tokens()
    override val tokens: List<Token> = listOf(token.MATIC, token.WMATIC, token.USDT)
    
    override val nativeCurrency: DecimalsNativeToken = token.MATIC
    override val swapBases: List<Erc20Token> = listOf(token.WMATIC)
    override val dexEndpoints: List<DexEndpoint> = listOf(
        DexEndpoint.hardcoded(
            factoryContractAddress = "0x8a628F00710993c1cebbaa02338d2264ee7056C6",
            initCodeHash = "0x85f8ad645fe62917d6939782650649d3d7c4b5f25d81415a9fac4a9f341793ca",
            liquidityProviderFee = 0.002
        )
    )

    inner class Tokens internal constructor(){
        val WMATIC = DecimalsErc20Token(
            network = this@PolygonMumbai,
            tokenAddress = "0x9c3C9283D3e44854697Cd22D3Faa240Cfb032889",
            decimals = 18
        )
        val USDT = DecimalsErc20Token(
            network = this@PolygonMumbai,
            tokenAddress = "0x9a01bf917477dd9f5d715d188618fc8b7350cd22",
            decimals = 6
        )
        val MATIC = DecimalsNativeToken(
            network = this@PolygonMumbai,
            wrapped = WMATIC
        )

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            com.symbiosis.sdk.currency.DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)
    }
}
