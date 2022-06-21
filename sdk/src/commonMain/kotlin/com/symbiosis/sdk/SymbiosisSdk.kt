package com.symbiosis.sdk

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
    web3Provider: (String) -> Web3Executor = ::Web3
) = SymbiosisSdk(
    avalancheMainnetExecutor = web3Provider(avalancheMainnetUrl),
    bscMainnetExecutor = web3Provider(bscMainnetUrl),
    ethMainnetExecutor = web3Provider(ethMainnetUrl),
    polygonMainnetExecutor = web3Provider(polygonMainnetUrl),
    bobaMainnetExecutor = web3Provider(bobaMainnetUrl),
    avalancheFujiExecutor = web3Provider(avalancheFujiUrl),
    bscTestnetExecutor = web3Provider(bscTestnetUrl),
    ethRinkebyExecutor = web3Provider(ethRinkebyUrl),
    polygonMumbaiExecutor = web3Provider(polygonMumbaiUrl),
    bobaRinkebyExecutor = web3Provider(bobaRinkebyUrl)
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
