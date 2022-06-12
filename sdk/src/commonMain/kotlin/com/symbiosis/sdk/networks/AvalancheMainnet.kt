package com.symbiosis.sdk.networks

import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.DecimalsNativeToken
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

class AvalancheMainnet(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(endpointUrl))

    override val maxBlocksPerRequestInt = 2048

    override val networkName: String = "AvalancheMainnet"

    override val chainIdInt = 0xA86A
    override val synthFabricAddressString = "0x947a0d452b40013190295a4151A090E1638Fb848"
    override val portalAddressString = "0xD7F9989bE0d15319d13d6FA5d468211C89F0b147"
    override val synthesizeAddressString = "0xb80fDAA74dDA763a8A158ba85798d373A5E84d84"
    override val bridgeAddressString = "0xd5F0f8dB993D26F5df89E70a83d32b369DcCdaa0"
    override val routerAddressString = "0xE54Ca86531e17Ef3616d22Ca28b0D458b6C89106"
    override val metaRouterAddressString = "0xE5E68630B5B759e6C701B70892AA8324b71e6e20"
    override val metaRouterGatewayAddressString = "0x25821A21C2E3455967229cADCA9b6fdd4A80a40b"

    val token = Tokens()
    override val tokens: List<DecimalsToken> = listOf(token.AVAX, token.WAVAX, token.USDC)

    override val nativeCurrency = token.AVAX
    override val uniSwapBases: List<DecimalsErc20Token> = listOf(token.USDC, token.WAVAX)

    override val dexEndpoints: List<DexEndpoint> = listOf(
        DexEndpoint.hardcoded(
            factoryContractAddress = "0xefa94DE7a4656D787667C749f7E1223D71E9FD88",
            initCodeHash = "0x40231f6b438bce0797c9ada29b718a87ea0a5cea3fe9a771abdd76bd41a3e545",
            liquidityProviderFee = 0.003
        )
    )

    inner class Tokens internal constructor() {
        val USDC = DecimalsErc20Token(
            network = this@AvalancheMainnet,
            tokenAddress = "0xA7D7079b0FEaD91F3e65f86E8915Cb59c1a4C664",
            decimals = 6
        )
        val WAVAX = DecimalsErc20Token(
            network = this@AvalancheMainnet,
            tokenAddress = "0xB31f66AA3C1e785363F0875A1B74E27b85FD66c7",
            decimals = 18
        )
        val AVAX = DecimalsNativeToken(WAVAX)

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)
    }
}
