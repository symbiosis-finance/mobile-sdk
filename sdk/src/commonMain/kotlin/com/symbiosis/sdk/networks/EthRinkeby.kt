@file:Suppress("PropertyName")

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

open class EthRinkeby(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(endpointUrl))

    override val networkName: String = "EthRinkeby"

    override val chainIdInt = 0x4
    override val synthFabricAddressString = "0x9A857D526A9e53697a9Df5fFc40bCCD70E7A0388"
    override val portalAddressString = "0xc7F1A6768B16De4BB15c146fd5030cD9F50533ab"
    override val synthesizeAddressString = "0xA9E177ff9c88b1DF688AaB02C599F0c24e895f0f"
    override val bridgeAddressString = "0x09256eCAdb6ca96D1d7Fd96280cfA38D5F4E0c4C"
    override val routerAddressString = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D"
    override val metaRouterAddressString = "0xaEF8DEfDBca28A3dADb510fc861aa105e51160Eb"

    val token = Tokens()
    override val tokens: List<Token> = listOf(token.ETH, token.WETH, token.USDC, token.UNI)

    override val nativeCurrency: DecimalsNativeToken = token.ETH
    override val swapBases: List<Erc20Token> = listOf(token.WETH)
    override val dexEndpoints: List<DexEndpoint> = listOf(
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
        val ETH = DecimalsNativeToken(
            network = this@EthRinkeby,
            wrapped = WETH
        )

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)
    }
}