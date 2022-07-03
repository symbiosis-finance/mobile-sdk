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
import com.symbiosis.sdk.dex.DexEndpoint.Companion.hardcoded as DexEndpoint

open class BobaRinkeby(override val executor: Web3Executor) : DefaultNetwork() {
    constructor(endpointUrl: String) : this(Web3(CHAIN_ID, endpointUrl))

    override val networkName = "BobaRinkeby"

    override val synthFabricAddressString = "0x042cF6a0690C9B8607c5B19Cb18807F1D66c9339"
    override val portalAddressString = "0x97A5B271421b443b3F53F3DF485B2716Db85fA4b"
    override val synthesizeAddressString = "0x97E82de1102C8Bd7687FFce4C51303D985fcc26e"
    override val bridgeAddressString = "0x9F7804105549F31098dCa61A22Bcb0671B78224C"
    override val routerAddressString = "0x4df04E20cCd9a8B82634754fcB041e86c5FF085A"
    override val metaRouterAddressString = "0x7d8B7b5f663E93D7F8970d0A61081Af03c63bB86"
    override val metaRouterGatewayAddressString = "0x24a3C8C385405E6673e69EAda362b8e82771BD77"

    val token = Tokens()

    override val tokens: List<DecimalsToken> = listOf(token.ETH, token.WETH, token.USDC)
    override val nativeCurrency: DecimalsNativeToken = token.ETH
    override val uniSwapBases: List<DecimalsErc20Token> = listOf(token.USDC, token.WETH)
    override val dexEndpoints: List<DexEndpoint> = listOf(
        DexEndpoint(
            factoryContractAddress = "0xab740666e226cb5b6b451eb943b0257a7cb3ce0a",
            initCodeHash = "0x1db9efb13a1398e31bb71895c392fa1217130f78dc65080174491adcec5da9b9",
            liquidityProviderFee = 0.003
        )
    )

    inner class Tokens internal constructor() {
        val USDC = DecimalsErc20Token(
            network = this@BobaRinkeby,
            tokenAddress = "0xB24898De59C8E259F9742bCF2C16Fd613DCeA8F7",
            decimals = 6
        )
        val WETH = DecimalsErc20Token(
            network = this@BobaRinkeby,
            tokenAddress = "0xDeadDeAddeAddEAddeadDEaDDEAdDeaDDeAD0000",
            decimals = 18
        )
        val ETH = DecimalsNativeToken(WETH)

        private fun DecimalsErc20Token(network: Network, tokenAddress: String, decimals: Int) =
            DecimalsErc20Token(network, ContractAddress(tokenAddress), decimals)
    }
    
    companion object {
        val CHAIN_ID = 0x1C.bi
    }
}
