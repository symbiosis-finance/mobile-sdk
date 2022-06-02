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
import com.symbiosis.sdk.dex.DexEndpoint.Companion.hardcoded as DexEndpoint

open class BobaMainnet(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(endpointUrl))

    override val networkName: String = "BobaMainnet"

    override val chainIdInt: Int = 0x120
    override val synthFabricAddressString = "0x947a0d452b40013190295a4151A090E1638Fb848"
    override val portalAddressString = "0xD7F9989bE0d15319d13d6FA5d468211C89F0b147"
    override val synthesizeAddressString = "0xb80fDAA74dDA763a8A158ba85798d373A5E84d84"
    override val bridgeAddressString = "0xd5F0f8dB993D26F5df89E70a83d32b369DcCdaa0"
    override val routerAddressString = "0x17C83E2B96ACfb5190d63F5E46d93c107eC0b514"
    override val metaRouterAddressString = "0xd2B5945829D8254C40f63f476C9F02CF5762F8DF"
    override val metaRouterGatewayAddressString = "0x5ee04643fe2D63f364F77B38C41F15A54930f5C1"

    val token = Tokens()
    override val tokens: List<Token> = listOf(token.USDC, token.ETH, token.WETH)

    override val nativeCurrency: DecimalsNativeToken = token.ETH
    override val swapBases: List<Erc20Token> = listOf(token.WETH, token.USDC)
    override val dexEndpoints: List<DexEndpoint> = listOf(
        DexEndpoint(
            factoryContractAddress = "0x7DDaF116889D655D1c486bEB95017a8211265d29",
            initCodeHash = "0x1db9efb13a1398e31bb71895c392fa1217130f78dc65080174491adcec5da9b9",
            liquidityProviderFee = 0.003
        )
    )

    inner class Tokens internal constructor() {
        val USDC = DecimalsErc20Token(
            network = this@BobaMainnet,
            tokenAddress = "0x66a2A913e447d6b4BF33EFbec43aAeF87890FBbc",
            decimals = 6
        )
        val WETH = DecimalsErc20Token(
            network = this@BobaMainnet,
            tokenAddress = "0xDeadDeAddeAddEAddeadDEaDDEAdDeaDDeAD0000",
            decimals = 18
        )
        val ETH = DecimalsNativeToken(WETH)

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)
    }
}