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

open class HecoTestnet(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String = "https://http-testnet.hecochain.com") : this(Web3(endpointUrl))

    override val networkName: String = "HecoTestnet"

    override val chainIdInt = 0x100
    override val synthFabricAddressString = "0xd655C2c9D558Bf8E3382f98eDADb84e866665139"
    override val portalAddressString = "0x5302358dCFbF2881e5b5E537316786d8Ea242008"
    override val synthesizeAddressString = "0x3A54a6Ab296726691c06e325eEAa1F123c469531"
    override val bridgeAddressString = "0xcC9DBA9AF9ce104e150654B21436584b8e74b236"
    override val routerAddressString = "0x30a84231DC64848969da4aF755864382923a40f0"
    override val metaRouterAddressString = "0x81d80bEcFD9a6A64bB0602d79E8Bd45844083626"

    val token = Tokens()
    override val tokens: List<Token> = listOf(token.HT, token.WHT, token.HUSD)

    override val nativeCurrency: DecimalsNativeToken get() = token.HT
    override val swapBases: List<Erc20Token> = listOf(token.WHT)
    override val dexEndpoints: List<DexEndpoint> = listOf(
        DexEndpoint.hardcoded(
            factoryContractAddress = "0xca33f6D096BDD7FcB28d708f631cD76E73Ecfc2d",
            initCodeHash = "0x85f8ad645fe62917d6939782650649d3d7c4b5f25d81415a9fac4a9f341793ca",
            liquidityProviderFee = 0.002
        )
    )

    inner class Tokens internal constructor() {
        val WHT = DecimalsErc20Token(
            network = this@HecoTestnet,
            tokenAddress = "0x7aF326B6351C8A9b8fb8CD205CBe11d4Ac5FA836",
            decimals = 18
        )
        val HUSD = DecimalsErc20Token(
            network = this@HecoTestnet,
            tokenAddress = "0x41b5984f45AfB2560a0ED72bB69A98E8b32B3cCA",
            decimals = 18
        )
        val HT = DecimalsNativeToken(
            network = this@HecoTestnet,
            wrapped = WHT
        )

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)
    }
}
