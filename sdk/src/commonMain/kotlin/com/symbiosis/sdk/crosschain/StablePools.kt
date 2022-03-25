package com.symbiosis.sdk.crosschain

import com.symbiosis.sdk.networks.AvalancheFuji
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.networks.HecoTestnet
import com.symbiosis.sdk.networks.PolygonMumbai
import com.symbiosis.sdk.swap.meta.NerveStablePool
import dev.icerock.moko.web3.ContractAddress

object StablePools {
    fun BUSD_sUSDC_POOL(bscTestnet: BscTestnet, ethRinkeby: EthRinkeby) = NerveStablePool(
        address = ContractAddress(value = "0x1c07E3Ef949C197576e4875798211Ef1d9AD4c4A"),
        fromToken = bscTestnet.token.BUSD,
        targetToken = ethRinkeby.token.USDC
    )

    // fixme: there is an old address
    // MUMBAI USDT -> sUSDC
    fun MUMBAI_USDT_sUSDC_POOL(polygonMumbai: PolygonMumbai, ethRinkeby: EthRinkeby) = NerveStablePool(
        address = ContractAddress(value = "0xEBfcF756A87C352195eA326Cd20afbc79aBecc9E"),
        fromToken = polygonMumbai.token.USDT,
        targetToken = ethRinkeby.token.USDC
    )

    // fixme: there is an old address
    // MUMBAI USDT -> sBUSD
    fun MUMBAI_USDT_sBUSD_POOL(polygonMumbai: PolygonMumbai, bscTestnet: BscTestnet) = NerveStablePool(
        address = ContractAddress(value = "0x919e11b43aAC4EeC39108F9931BAb0551C57E2B6"),
        fromToken = polygonMumbai.token.USDT,
        targetToken = bscTestnet.token.BUSD
    )

    // FUJI USDT -> sUSDC
    fun FUJI_USDT_sUSDC_POOL(avalancheFuji: AvalancheFuji, ethRinkeby: EthRinkeby) = NerveStablePool(
        address = ContractAddress(value = "0x23f5e7756A46e8744f653da9a476705216eb5FF8"),
        fromToken = avalancheFuji.token.USDT,
        targetToken = ethRinkeby.token.USDC
    )

    // FUJI USDT -> sBUSD
    fun FUJI_USDT_sBUSD_POOL(avalancheFuji: AvalancheFuji, bscTestnet: BscTestnet) = NerveStablePool(
        address = ContractAddress(value = "0x080B2e8E9EE50031D9EbcDEE4Cf920572B038419"),
        fromToken = avalancheFuji.token.USDT,
        targetToken = bscTestnet.token.BUSD
    )

    // HUOBI USDT -> sUSDC
    fun HUOBI_HUSD_sUSDC_POOL(hecoTestnet: HecoTestnet, ethRinkeby: EthRinkeby) = NerveStablePool(
        address = ContractAddress(value = "0x881E6e8b362f8eF20488bF7BD87cB8459A2dbCdD"),
        fromToken = hecoTestnet.token.HUSD,
        targetToken = ethRinkeby.token.USDC
    )
}
