package com.symbiosis.sdk

import com.symbiosis.sdk.crosschain.CrossChain
import com.symbiosis.sdk.crosschain.CrossChainClient
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.network.NetworkClient
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
    bobaRinkebyUrl: String = "https://rinkeby.boba.network/"
) = SymbiosisSdk(
    avalancheMainnetExecutor = Web3(avalancheMainnetUrl),
    bscMainnetExecutor = Web3(bscMainnetUrl),
    ethMainnetExecutor = Web3(ethMainnetUrl),
    polygonMainnetExecutor = Web3(polygonMainnetUrl),
    bobaMainnetExecutor = Web3(bobaMainnetUrl),
    avalancheFujiExecutor = Web3(avalancheFujiUrl),
    bscTestnetExecutor = Web3(bscTestnetUrl),
    ethRinkebyExecutor = Web3(ethRinkebyUrl),
    polygonMumbaiExecutor = Web3(polygonMumbaiUrl),
    bobaRinkebyExecutor = Web3(bobaRinkebyUrl)
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

        // default implementation is always used when calling this function, so
        // ClientsManager.* methods will have the default implementation
        override fun getCrossChainClient(crossChain: CrossChain): CrossChainClient =
            mainnet.getCrossChainClient(crossChain)

        override fun getNetworkClient(network: Network): NetworkClient =
            mainnet.getNetworkClient(network)
    }
