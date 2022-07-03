@file:Suppress("PropertyName")

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

open class EthRinkeby(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(CHAIN_ID, endpointUrl))

    override val networkName: String = "EthRinkeby"

    override val synthFabricAddressString = "0x9A857D526A9e53697a9Df5fFc40bCCD70E7A0388"
    override val portalAddressString = "0xc7F1A6768B16De4BB15c146fd5030cD9F50533ab"
    override val synthesizeAddressString = "0xA9E177ff9c88b1DF688AaB02C599F0c24e895f0f"
    override val bridgeAddressString = "0x09256eCAdb6ca96D1d7Fd96280cfA38D5F4E0c4C"
    override val routerAddressString = "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D"
    override val metaRouterAddressString = "0x420063Aa9DC201038bacAbe4F1A987b293eED3C0"
    override val metaRouterGatewayAddressString = "0xeb4edB23fd2e80920112372a2f54549b64a6d203"

    val token = Tokens()
    override val tokens: List<DecimalsToken> = listOf(token.ETH, token.WETH, token.USDC, token.UNI)

    override val nativeCurrency = token.ETH
    override val uniSwapBases = listOf(token.WETH)
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
    
    companion object {
        val CHAIN_ID = 0x4.bi
    }
}