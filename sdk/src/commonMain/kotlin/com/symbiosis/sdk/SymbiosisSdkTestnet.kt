@file:Suppress("MemberVisibilityCanBePrivate")

package com.symbiosis.sdk

import com.symbiosis.sdk.crosschain.CrossChainClient
import com.symbiosis.sdk.crosschain.testnet.*
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.networks.*
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

interface SymbiosisSdkTestnet : ClientsManager {
    val avalancheFuji: AvalancheFuji
    val bscTestnet: BscTestnet
    val ethRinkeby: EthRinkeby
    val polygonMumbai: PolygonMumbai
    val bobaRinkeby: BobaRinkeby

    override val allNetworks: List<Network>
    override val allTokens: List<Token>

    val avalancheFujiClient: NetworkClient
    val bscTestnetClient: NetworkClient
    val ethRinkebyClient: NetworkClient
    val polygonMumbaiClient: NetworkClient
    val bobaRinkebyClient: NetworkClient

    override val allClients: List<NetworkClient>

    val avalancheFujiBscTestnetClient: CrossChainClient
    val avalancheFujiEthRinkebyClient: CrossChainClient
    val bscTestnetAvalancheFujiClient: CrossChainClient
    val bscTestnetEthRinkebyClient: CrossChainClient
    val bscTestnetPolygonMumbaiClient: CrossChainClient
    val bscTestnetBobaRinkebyClient: CrossChainClient
    val ethRinkebyAvalancheFujiClient: CrossChainClient
    val ethRinkebyBscTestnetClient: CrossChainClient
    val ethRinkebyPolygonMumbaiClient: CrossChainClient
    val ethRinkebyBobaRinkebyClient: CrossChainClient
    val polygonMumbaiBscTestnetClient: CrossChainClient
    val polygonMumbaiEthRinkebyClient: CrossChainClient
    val bobaRinkebyEthRinkebyClient: CrossChainClient
    val bobaRinkebyBscTestnetClient: CrossChainClient

    override val allCrossChainClients: List<CrossChainClient>
}

fun SymbiosisSdkTestnet(
    bscTestnetUrl: String,
    ethRinkebyUrl: String,
    polygonMumbaiUrl: String,
    avalancheFujiUrl: String = "https://api.avax-test.network/ext/bc/C/rpc",
    bobaRinkebyUrl: String = "https://rinkeby.boba.network/"
) = SymbiosisSdkTestnet(
    avalancheFujiExecutor = Web3(avalancheFujiUrl),
    bscTestnetExecutor = Web3(bscTestnetUrl),
    ethRinkebyExecutor = Web3(ethRinkebyUrl),
    polygonMumbaiExecutor = Web3(polygonMumbaiUrl),
    bobaRinkebyExecutor = Web3(bobaRinkebyUrl)
)

fun SymbiosisSdkTestnet(
    avalancheFujiExecutor: Web3Executor,
    bscTestnetExecutor: Web3Executor,
    ethRinkebyExecutor: Web3Executor,
    polygonMumbaiExecutor: Web3Executor,
    bobaRinkebyExecutor: Web3Executor
): SymbiosisSdkTestnet = object : SymbiosisSdkTestnet {
    override val avalancheFuji = AvalancheFuji(avalancheFujiExecutor)
    override val bscTestnet = BscTestnet(bscTestnetExecutor)
    override val ethRinkeby = EthRinkeby(ethRinkebyExecutor)
    override val polygonMumbai = PolygonMumbai(polygonMumbaiExecutor)
    override val bobaRinkeby = BobaRinkeby(bobaRinkebyExecutor)

    override val allNetworks: List<DefaultNetwork> = listOf(
        avalancheFuji, bscTestnet,
        ethRinkeby, polygonMumbai,
        bobaRinkeby
    )
    override val allTokens: List<Token> = allNetworks.flatMap(DefaultNetwork::tokens)

    override val avalancheFujiClient = getNetworkClient(avalancheFuji)
    override val bscTestnetClient = getNetworkClient(bscTestnet)
    override val ethRinkebyClient = getNetworkClient(ethRinkeby)
    override val polygonMumbaiClient = getNetworkClient(polygonMumbai)
    override val bobaRinkebyClient = getNetworkClient(bobaRinkeby)

    override val allClients: List<NetworkClient> = listOf(
        avalancheFujiClient, bscTestnetClient,
        ethRinkebyClient, polygonMumbaiClient,
        bobaRinkebyClient
    )

    override val avalancheFujiBscTestnetClient =
        getCrossChainClient(AvalancheFujiBscTestnet(avalancheFujiExecutor, bscTestnetExecutor))
    override val avalancheFujiEthRinkebyClient =
        getCrossChainClient(AvalancheFujiEthRinkeby(avalancheFujiExecutor, ethRinkebyExecutor))
    override val bscTestnetAvalancheFujiClient =
        getCrossChainClient(BscTestnetAvalancheFuji(bscTestnetExecutor, avalancheFujiExecutor))
    override val bscTestnetEthRinkebyClient =
        getCrossChainClient(BscTestnetEthRinkeby(bscTestnetExecutor, ethRinkebyExecutor))
    override val bscTestnetPolygonMumbaiClient =
        getCrossChainClient(BscTestnetPolygonMumbai(bscTestnetExecutor, polygonMumbaiExecutor))
    override val bscTestnetBobaRinkebyClient =
        getCrossChainClient(BscTestnetBobaRinkeby(bscTestnetExecutor, bobaRinkebyExecutor))
    override val ethRinkebyAvalancheFujiClient =
        getCrossChainClient(EthRinkebyAvalancheFuji(ethRinkebyExecutor, avalancheFujiExecutor))
    override val ethRinkebyBscTestnetClient =
        getCrossChainClient(EthRinkebyBscTestnet(ethRinkebyExecutor, bscTestnetExecutor))
    override val ethRinkebyPolygonMumbaiClient =
        getCrossChainClient(EthRinkebyPolygonMumbai(ethRinkebyExecutor, polygonMumbaiExecutor))
    override val ethRinkebyBobaRinkebyClient =
        getCrossChainClient(EthRinkebyBobaRinkeby(ethRinkebyExecutor, bobaRinkebyExecutor))
    override val polygonMumbaiBscTestnetClient =
        getCrossChainClient(PolygonMumbaiBscTestnet(polygonMumbaiExecutor, bscTestnetExecutor))
    override val polygonMumbaiEthRinkebyClient =
        getCrossChainClient(PolygonMumbaiEthRinkeby(polygonMumbaiExecutor, ethRinkebyExecutor))
    override val bobaRinkebyEthRinkebyClient =
        getCrossChainClient(BobaRinkebyEthRinkeby(bobaRinkebyExecutor, ethRinkebyExecutor))
    override val bobaRinkebyBscTestnetClient =
        getCrossChainClient(BobaRinkebyBscTestnet(bobaRinkebyExecutor, bscTestnetExecutor))

    override val allCrossChainClients: List<CrossChainClient> = listOf(
        avalancheFujiBscTestnetClient, avalancheFujiEthRinkebyClient,
        bscTestnetAvalancheFujiClient, bscTestnetEthRinkebyClient,
        bscTestnetPolygonMumbaiClient, ethRinkebyAvalancheFujiClient,
        ethRinkebyBscTestnetClient, ethRinkebyPolygonMumbaiClient,
        polygonMumbaiBscTestnetClient, polygonMumbaiEthRinkebyClient,
        bscTestnetBobaRinkebyClient, ethRinkebyBobaRinkebyClient,
        bobaRinkebyEthRinkebyClient, bobaRinkebyBscTestnetClient
    )
}

