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

class PolygonMumbai(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(endpointUrl))

    override val networkName: String = "PolygonMumbai"

    override val chainIdInt = 0x13881
    override val synthFabricAddressString = "0xe0A0CEb6f3e740C4fc4A1eb38e4135440470175A"
    override val portalAddressString = "0xfF0a032e793bAf78C4153CD96135013D6A468b39"
    override val synthesizeAddressString = "0xEE0F117Db9ED4d1A4421cdCa7d32a1F878eF4F7C"
    override val bridgeAddressString = "0xEdCAeb1D346396B3e4E861e4A6F0B72b9850fCC5"
    override val routerAddressString = "0xca33f6D096BDD7FcB28d708f631cD76E73Ecfc2d"
    override val metaRouterAddressString = "0xBD52F6fF61ce247798602B8F794a2DC31a8aB5F9"
    override val metaRouterGatewayAddressString = "0xF7Bc9b805d94F47b5A7BacF9fb847b1d2D830f60"

    val token = Tokens()
    override val tokens: List<Token> = listOf(token.MATIC, token.WMATIC, token.USDT)
    
    override val nativeCurrency: DecimalsNativeToken = token.MATIC
    override val swapBases: List<Erc20Token> = listOf(token.WMATIC)
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
}
