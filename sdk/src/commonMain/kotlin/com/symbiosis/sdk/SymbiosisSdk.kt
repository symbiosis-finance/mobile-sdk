package com.symbiosis.sdk

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.networks.AvalancheFuji
import com.symbiosis.sdk.networks.AvalancheMainnet
import com.symbiosis.sdk.networks.BobaMainnet
import com.symbiosis.sdk.networks.BobaRinkeby
import com.symbiosis.sdk.networks.BscMainnet
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.networks.EthMainnet
import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.networks.PolygonMainnet
import com.symbiosis.sdk.networks.PolygonMumbai
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

interface SymbiosisSdk : SymbiosisSdkMainnet, SymbiosisSdkTestnet

fun SymbiosisSdk(
    bscMainnetUrl: String,
    ethMainnetUrl: String,
    polygonMainnetUrl: String,
    bscTestnetUrl: String,
    ethRinkebyUrl: String,
    polygonMumbaiUrl: String,
    avalancheMainnetUrl: String = "https://api.avax.network/ext/bc/C/rpc",
    bobaMainnetUrl: String = "https://mainnet.boba.network/",
    avalancheFujiUrl: String = "https://api.avax-test.network/ext/bc/C/rpc",
    bobaRinkebyUrl: String = "https://rinkeby.boba.network/",
    web3Provider: (chainId: BigInt, url: String) -> Web3Executor = ::Web3
) = SymbiosisSdk(
    avalancheMainnetExecutor = web3Provider(AvalancheMainnet.CHAIN_ID, avalancheMainnetUrl),
    bscMainnetExecutor = web3Provider(BscMainnet.CHAIN_ID, bscMainnetUrl),
    ethMainnetExecutor = web3Provider(EthMainnet.CHAIN_ID, ethMainnetUrl),
    polygonMainnetExecutor = web3Provider(PolygonMainnet.CHAIN_ID, polygonMainnetUrl),
    bobaMainnetExecutor = web3Provider(BobaMainnet.CHAIN_ID, bobaMainnetUrl),
    avalancheFujiExecutor = web3Provider(AvalancheFuji.CHAIN_ID, avalancheFujiUrl),
    bscTestnetExecutor = web3Provider(BscTestnet.CHAIN_ID, bscTestnetUrl),
    ethRinkebyExecutor = web3Provider(EthRinkeby.CHAIN_ID, ethRinkebyUrl),
    polygonMumbaiExecutor = web3Provider(PolygonMumbai.CHAIN_ID, polygonMumbaiUrl),
    bobaRinkebyExecutor = web3Provider(BobaRinkeby.CHAIN_ID, bobaRinkebyUrl)
)

fun SymbiosisSdk(
    avalancheMainnetExecutor: Web3Executor,
    bscMainnetExecutor: Web3Executor,
    ethMainnetExecutor: Web3Executor,
    polygonMainnetExecutor: Web3Executor,
    bobaMainnetExecutor: Web3Executor,
    avalancheFujiExecutor: Web3Executor,
    bscTestnetExecutor: Web3Executor,
    ethRinkebyExecutor: Web3Executor,
    polygonMumbaiExecutor: Web3Executor,
    bobaRinkebyExecutor: Web3Executor
): SymbiosisSdk = createSymbiosisSdk(
    mainnet = SymbiosisSdkMainnet(
        avalancheMainnetExecutor, bscMainnetExecutor,
        ethMainnetExecutor, polygonMainnetExecutor, bobaMainnetExecutor
    ),
    testnet = SymbiosisSdkTestnet(
        avalancheFujiExecutor, bscTestnetExecutor,
        ethRinkebyExecutor, polygonMumbaiExecutor, bobaRinkebyExecutor
    )
)

private fun createSymbiosisSdk(mainnet: SymbiosisSdkMainnet, testnet: SymbiosisSdkTestnet): SymbiosisSdk =
    object : SymbiosisSdk, SymbiosisSdkMainnet by mainnet, SymbiosisSdkTestnet by testnet {
        override val allNetworks = mainnet.allNetworks + testnet.allNetworks
        override val allTokens = mainnet.allTokens + testnet.allTokens
        override val allClients = mainnet.allClients + testnet.allClients
        override val allCrossChainClients = mainnet.allCrossChainClients + testnet.allCrossChainClients
    }
