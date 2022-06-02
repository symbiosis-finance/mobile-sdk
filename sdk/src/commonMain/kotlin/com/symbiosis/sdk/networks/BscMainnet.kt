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

class BscMainnet(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(endpointUrl))

    override val networkName = "BscMainnet"

    override val chainIdInt = 0x38
    override val synthFabricAddressString = "0x947a0d452b40013190295a4151A090E1638Fb848"
    override val portalAddressString = "0xD7F9989bE0d15319d13d6FA5d468211C89F0b147"
    override val synthesizeAddressString = "0xb80fDAA74dDA763a8A158ba85798d373A5E84d84"
    override val bridgeAddressString = "0xd5F0f8dB993D26F5df89E70a83d32b369DcCdaa0"
    override val routerAddressString = "0x10ED43C718714eb63d5aA57B78B54704E256024E"
    override val metaRouterAddressString = "0x8D602356c7A6220CDE24BDfB4AB63EBFcb0a9b5d"
    override val metaRouterGatewayAddressString = "0xe2faC824615538C3A9ae704c75582cD1AbdD7cdf"

    val token = Tokens()
    override val tokens: List<Token> = listOf(token.BNB, token.WBNB, token.BUSD, token.CAKE)

    override val nativeCurrency = token.BNB
    override val swapBases: List<Erc20Token> = listOf(token.WBNB, token.CAKE, token.BUSD)
    override val dexEndpoints: List<DexEndpoint> = listOf(
        DexEndpoint.hardcoded(
            factoryContractAddress = "0xBCfCcbde45cE874adCB698cC183deBcF17952812",
            initCodeHash = "0xd0d4c4cd0848c93cb4fd1f498d7013ee6bfb25783ea21593d5834f5d250ece66",
            liquidityProviderFee = 0.002
        )
    )

    inner class Tokens internal constructor() {
        val CAKE = DecimalsErc20Token(
            network = this@BscMainnet,
            tokenAddress = "0x0e09fabb73bd3ade0a17ecc321fd13a19e81ce82",
            decimals = 18
        )
        val BUSD = DecimalsErc20Token(
            network = this@BscMainnet,
            tokenAddress = "0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56",
            decimals = 18
        )
        val WBNB = DecimalsErc20Token(
            network = this@BscMainnet,
            tokenAddress = "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c",
            decimals = 18
        )
        val BNB = DecimalsNativeToken(WBNB)

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)
    }
}