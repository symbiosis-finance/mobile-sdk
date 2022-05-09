@file:Suppress("FunctionName")

package com.symbiosis.sdk.crosschain

import com.symbiosis.sdk.networks.*
import com.symbiosis.sdk.swap.meta.NerveStablePool
import dev.icerock.moko.web3.ContractAddress

object StablePools {
    fun BSC_TESTNET_BUSD_ETH_RINKEBY_sUSDC_POOL(bscTestnet: BscTestnet, ethRinkeby: EthRinkeby) =
        NerveStablePool(
            address = ContractAddress(value = "0x1c07E3Ef949C197576e4875798211Ef1d9AD4c4A"),
            fromToken = bscTestnet.token.BUSD,
            targetToken = ethRinkeby.token.USDC
        )

    fun POLYGON_MUMBAI_USDT_ETH_RINKEBY_sUSDC_POOL(polygonMumbai: PolygonMumbai, ethRinkeby: EthRinkeby) =
        NerveStablePool(
            address = ContractAddress(value = "0xEBfcF756A87C352195eA326Cd20afbc79aBecc9E"),
            fromToken = polygonMumbai.token.USDT,
            targetToken = ethRinkeby.token.USDC
        )

    fun POLYGON_MUMBAI_USDT_BSC_TESTNET_sBUSD_POOL(polygonMumbai: PolygonMumbai, bscTestnet: BscTestnet) =
        NerveStablePool(
            address = ContractAddress(value = "0x919e11b43aAC4EeC39108F9931BAb0551C57E2B6"),
            fromToken = polygonMumbai.token.USDT,
            targetToken = bscTestnet.token.BUSD
        )

    // FUJI USDT -> sUSDC
    fun AVALANCHE_FUJI_USDT_ETH_RINKEBY_sUSDC_POOL(avalancheFuji: AvalancheFuji, ethRinkeby: EthRinkeby) =
        NerveStablePool(
            address = ContractAddress(value = "0x23f5e7756A46e8744f653da9a476705216eb5FF8"),
            fromToken = avalancheFuji.token.USDT,
            targetToken = ethRinkeby.token.USDC
        )

    // FUJI USDT -> sBUSD
    fun AVALANCHE_FUJI_USDT_BSC_TESTNET_sBUSD_POOL(avalancheFuji: AvalancheFuji, bscTestnet: BscTestnet) =
        NerveStablePool(
            address = ContractAddress(value = "0x080B2e8E9EE50031D9EbcDEE4Cf920572B038419"),
            fromToken = avalancheFuji.token.USDT,
            targetToken = bscTestnet.token.BUSD
        )

    fun BOBA_RINKEBY_USDC_ETH_RINKEBY_sUSDC_POOL(bobaRinkeby: BobaRinkeby, ethRinkeby: EthRinkeby) =
        NerveStablePool(
            address = ContractAddress("0xE3b9A85633cBc51eA96637A8606Eed0071f61627"),
            fromToken = bobaRinkeby.token.USDC,
            targetToken = ethRinkeby.token.USDC
        )

    fun BOBA_RINKEBY_USDC_BSC_TESTNET_sBUSD_POOL(bobaRinkeby: BobaRinkeby, bscTestnet: BscTestnet) =
        NerveStablePool(
            address = ContractAddress("0x04928B7bC6a946f998aD458016B8dA7280506376"),
            fromToken = bobaRinkeby.token.USDC,
            targetToken = bscTestnet.token.BUSD
        )

    fun AVALANCHE_MAINNET_USDC_ETH_MAINNET_sUSDC(avalancheMainnet: AvalancheMainnet, ethMainnet: EthMainnet) =
        NerveStablePool(
            address = ContractAddress("0xab0738320A21741f12797Ee921461C691673E276"),
            fromToken = avalancheMainnet.token.USDC,
            targetToken = ethMainnet.token.USDC
        )

    fun AVALANCHE_MAINNET_USDC_BSC_MAINNET_sBUSD(avalancheMainnet: AvalancheMainnet, bscMainnet: BscMainnet) =
        NerveStablePool(
            address = ContractAddress("0xF4BFF06E02cdF55918e0ec98082bDE1DA85d33Db"),
            fromToken = avalancheMainnet.token.USDC,
            targetToken = bscMainnet.token.BUSD
        )

    fun POLYGON_MAINNET_USDC_ETH_MAINNET_sUSDC(polygonMainnet: PolygonMainnet, ethMainnet: EthMainnet) =
        NerveStablePool(
            address = ContractAddress("0xab0738320A21741f12797Ee921461C691673E276"),
            fromToken = polygonMainnet.token.USDC,
            targetToken = ethMainnet.token.USDC
        )

    fun POLYGON_MAINNET_USDC_BSC_MAINNET_sBUSD(polygonMainnet: PolygonMainnet, bscMainnet: BscMainnet) =
        NerveStablePool(
            address = ContractAddress("0xF4BFF06E02cdF55918e0ec98082bDE1DA85d33Db"),
            fromToken = polygonMainnet.token.USDC,
            targetToken = bscMainnet.token.BUSD
        )

    fun POLYGON_MAINNET_USDC_AVALANCHE_MAINNET_sUSDC(polygonMainnet: PolygonMainnet, avalancheMainnet: AvalancheMainnet) =
        NerveStablePool(
            address = ContractAddress("0x3F1bfa6FA3B6D03202538Bf0cdE92BbE551104ac"),
            fromToken = polygonMainnet.token.USDC,
            targetToken = avalancheMainnet.token.USDC
        )

    fun BSC_MAINNET_BUSD_ETH_MAINNET_sUSDC(bscMainnet: BscMainnet, ethMainnet: EthMainnet) =
        NerveStablePool(
            address = ContractAddress("0xab0738320A21741f12797Ee921461C691673E276"),
            fromToken = bscMainnet.token.BUSD,
            targetToken = ethMainnet.token.USDC
        )

    fun BOBA_MAINNET_USDC_ETH_MAINNET_sUSDC(bobaMainnet: BobaMainnet, ethMainnet: EthMainnet) =
        NerveStablePool(
            address = ContractAddress("0xab0738320A21741f12797Ee921461C691673E276"),
            fromToken = bobaMainnet.token.USDC,
            targetToken = ethMainnet.token.USDC
        )

    fun BOBA_MAINNET_USDC_BSC_MAINNET_sBUSD(bobaMainnet: BobaMainnet, bscMainnet: BscMainnet) =
        NerveStablePool(
            address = ContractAddress("0xe0ddd7afC724BD4B320472B5C954c0abF8192344"),
            fromToken = bobaMainnet.token.USDC,
            targetToken = bscMainnet.token.BUSD
        )
}
