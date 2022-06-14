package com.symbiosis.sdk.networks

import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.DecimalsNativeToken
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class EthMainnet(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(endpointUrl))

    override val networkName = "EthMainnet"

    override val chainIdInt = 0x1
    override val synthFabricAddressString = "0x0000000000000000000000000000000000000000"
    override val portalAddressString = "0xb80fDAA74dDA763a8A158ba85798d373A5E84d84"
    override val synthesizeAddressString = "0x0000000000000000000000000000000000000000"
    override val bridgeAddressString = "0xd5F0f8dB993D26F5df89E70a83d32b369DcCdaa0"
    override val routerAddressString = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D"
    override val metaRouterAddressString = "0xB9E13785127BFfCc3dc970A55F6c7bF0844a3C15"
    override val metaRouterGatewayAddressString = "0x03B7551EB0162c838a10c2437b60D1f5455b9554"

    val token = Tokens()
    override val tokens: List<DecimalsToken> = listOf(token.ETH, token.WETH, token.USDC, token.UNI)

    override val nativeCurrency = token.ETH
    override val uniSwapBases = listOf(token.WETH, token.UNI, token.USDC)
    override val dexEndpoints = listOf(
        DexEndpoint.hardcoded(
            factoryContractAddress = "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f",
            initCodeHash = "0x96e8ac4277198ff8b6f785478aa9a39f403cb768dd02cbee326c3e7da348845f",
            liquidityProviderFee = 0.003
        )
    )

    inner class Tokens internal constructor() {
        val UNI = DecimalsErc20Token(
            network = this@EthMainnet,
            tokenAddress = "0x1f9840a85d5aF5bf1D1762F925BDADdC4201F984",
            decimals = 18
        )
        val USDC = DecimalsErc20Token(
            network = this@EthMainnet,
            tokenAddress = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
            decimals = 6
        )
        val WETH = DecimalsErc20Token(
            network = this@EthMainnet,
            tokenAddress = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
            decimals = 18
        )
        val ETH = DecimalsNativeToken(WETH)

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)
    }
}
