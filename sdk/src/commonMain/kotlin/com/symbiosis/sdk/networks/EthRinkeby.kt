@file:Suppress("PropertyName")

package com.symbiosis.sdk.networks

import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.DecimalsNativeToken
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

open class EthRinkeby(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(endpointUrl))

    override val networkName: String = "EthRinkeby"

    override val chainIdInt = 0x4
    override val synthFabricAddressString = "0xB5ec93b32320Adb12Eef81cB97B68a3C69f8bc4E"
    override val portalAddressString = "0x68801662cab0D678E5216CB67DaD350271375024"
    override val synthesizeAddressString = "0xBA7c80bb5d316c4eE55F96F47d1a1477fFD1aFb6"
    override val bridgeAddressString = "0x38b07a83b691bB221d0710B0eA6Ebd7494E106D3"
    override val routerAddressString = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D"
    override val metaRouterAddressString = "0x7f04Fca5e687bCB42987F800CcFa3Abe56DC871B"
    override val metaRouterGatewayAddressString = "0x94358460e21C69599B7a207885f91443B3794C7b"

    val token = Tokens()
    override val tokens: List<Token> = listOf(token.ETH, token.WETH, token.USDC, token.UNI)

    override val nativeCurrency = token.ETH
    override val swapBases = listOf(token.WETH)
    override val dexEndpoints = listOf(
        // UNI
        DexEndpoint.hardcoded(
            factoryContractAddress = "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f",
            initCodeHash = "0x96e8ac4277198ff8b6f785478aa9a39f403cb768dd02cbee326c3e7da348845f",
            liquidityProviderFee = 0.003
        )
    )

    inner class Tokens internal constructor() {
        val UNI = DecimalsErc20Token(
            network = this@EthRinkeby,
            tokenAddress = "0x1f9840a85d5af5bf1d1762f925bdaddc4201f984",
            decimals = 18
        )
        val USDC = DecimalsErc20Token(
            network = this@EthRinkeby,
            tokenAddress = "0x4DBCdF9B62e891a7cec5A2568C3F4FAF9E8Abe2b",
            decimals = 6
        )
        val WETH = DecimalsErc20Token(
            network = this@EthRinkeby,
            tokenAddress = "0xc778417e063141139fce010982780140aa0cd5ab",
            decimals = 18
        )
        val ETH = DecimalsNativeToken(WETH)

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)
    }
}