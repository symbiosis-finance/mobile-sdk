package com.symbiosis.sdk

import com.symbiosis.sdk.swap.crosschain.CrossChainClient
import com.symbiosis.sdk.crosschain.mainnet.*
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.networks.*
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

interface SymbiosisSdkMainnet : ClientsManager {
    val avalancheMainnet: AvalancheMainnet
    val bscMainnet: BscMainnet
    val ethMainnet: EthMainnet
    val polygonMainnet: PolygonMainnet
    val bobaMainnet: BobaMainnet

    override val allNetworks: List<Network>
    override val allTokens: List<Token>

    val avalancheMainnetClient: NetworkClient
    val bscMainnetClient: NetworkClient
    val ethMainnetClient: NetworkClient
    val polygonMainnetClient: NetworkClient
    val bobaMainnetClient: NetworkClient

    override val allClients: List<NetworkClient>

    val avalancheMainnetEthMainnetClient: CrossChainClient
    val avalancheMainnetBscMainnetClient: CrossChainClient
    val avalancheMainnetPolygonMainnetClient: CrossChainClient
    val ethMainnetAvalancheMainnetClient: CrossChainClient
    val ethMainnetBscMainnetClient: CrossChainClient
    val ethMainnetBobaMainnetClient: CrossChainClient
    val ethMainnetPolygonMainnetClient: CrossChainClient
    val bscMainnetAvalancheMainnetClient: CrossChainClient
    val bscMainnetPolygonMainnetClient: CrossChainClient
    val bscMainnetBobaMainnetClient: CrossChainClient
    val bscMainnetEthMainnetClient: CrossChainClient
    val bobaMainnetBscMainnetClient: CrossChainClient
    val bobaMainnetEthMainnetClient: CrossChainClient
    val polygonMainnetAvalancheMainnetClient: CrossChainClient
    val polygonMainnetEthMainnetClient: CrossChainClient
    val polygonMainnetBscMainnetClient: CrossChainClient

    override val allCrossChainClients: List<CrossChainClient>
}

fun SymbiosisSdkMainnet(
    bscMainnetUrl: String,
    ethMainnetUrl: String,
    polygonMainnetUrl: String,
    avalancheMainnetUrl: String = "https://api.avax.network/ext/bc/C/rpc",
    bobaMainnetUrl: String = "https://mainnet.boba.network/"
) = SymbiosisSdkMainnet(
    avalancheMainnetExecutor = Web3(avalancheMainnetUrl),
    bscMainnetExecutor = Web3(bscMainnetUrl),
    ethMainnetExecutor = Web3(ethMainnetUrl),
    polygonMainnetExecutor = Web3(polygonMainnetUrl),
    bobaMainnetExecutor = Web3(bobaMainnetUrl)
)

fun SymbiosisSdkMainnet(
    avalancheMainnetExecutor: Web3Executor,
    bscMainnetExecutor: Web3Executor,
    ethMainnetExecutor: Web3Executor,
    polygonMainnetExecutor: Web3Executor,
    bobaMainnetExecutor: Web3Executor
): SymbiosisSdkMainnet = object : SymbiosisSdkMainnet {
    override val avalancheMainnet: AvalancheMainnet = AvalancheMainnet(avalancheMainnetExecutor)
    override val bscMainnet: BscMainnet = BscMainnet(bscMainnetExecutor)
    override val ethMainnet: EthMainnet = EthMainnet(ethMainnetExecutor)
    override val polygonMainnet: PolygonMainnet = PolygonMainnet(polygonMainnetExecutor)
    override val bobaMainnet: BobaMainnet = BobaMainnet(bobaMainnetExecutor)

    override val allNetworks: List<DefaultNetwork> =
        listOf(avalancheMainnet, bscMainnet, ethMainnet, polygonMainnet, bobaMainnet)
    override val allTokens: List<Token> = allNetworks.flatMap(DefaultNetwork::tokens)

    override val avalancheMainnetClient: NetworkClient = getNetworkClient(avalancheMainnet)
    override val bscMainnetClient: NetworkClient = getNetworkClient(bscMainnet)
    override val ethMainnetClient: NetworkClient = getNetworkClient(ethMainnet)
    override val polygonMainnetClient: NetworkClient = getNetworkClient(polygonMainnet)
    override val bobaMainnetClient: NetworkClient = getNetworkClient(bobaMainnet)

    override val allClients: List<NetworkClient> = listOf(
        avalancheMainnetClient, bscMainnetClient,
        ethMainnetClient, polygonMainnetClient,
        bobaMainnetClient
    )

    override val avalancheMainnetEthMainnetClient =
        getCrossChainClient(AvalancheMainnetEthMainnet(avalancheMainnetExecutor, ethMainnetExecutor))
    override val avalancheMainnetBscMainnetClient =
        getCrossChainClient(AvalancheMainnetBscMainnet(avalancheMainnetExecutor, bscMainnetExecutor))
    override val avalancheMainnetPolygonMainnetClient =
        getCrossChainClient(AvalancheMainnetPolygonMainnet(avalancheMainnetExecutor, polygonMainnetExecutor))
    override val ethMainnetAvalancheMainnetClient =
        getCrossChainClient(EthMainnetAvalancheMainnet(ethMainnetExecutor, avalancheMainnetExecutor))
    override val ethMainnetBscMainnetClient =
        getCrossChainClient(EthMainnetBscMainnet(ethMainnetExecutor, bscMainnetExecutor))
    override val ethMainnetBobaMainnetClient =
        getCrossChainClient(EthMainnetBobaMainnet(ethMainnetExecutor, bobaMainnetExecutor))
    override val ethMainnetPolygonMainnetClient =
        getCrossChainClient(EthMainnetPolygonMainnet(ethMainnetExecutor, polygonMainnetExecutor))
    override val bscMainnetAvalancheMainnetClient =
        getCrossChainClient(BscMainnetAvalancheMainnet(bscMainnetExecutor, avalancheMainnetExecutor))
    override val bscMainnetPolygonMainnetClient =
        getCrossChainClient(BscMainnetPolygonMainnet(bscMainnetExecutor, polygonMainnetExecutor))
    override val bscMainnetBobaMainnetClient =
        getCrossChainClient(BscMainnetBobaMainnet(bscMainnetExecutor, bobaMainnetExecutor))
    override val bscMainnetEthMainnetClient =
        getCrossChainClient(BscMainnetEthMainnet(bscMainnetExecutor, ethMainnetExecutor))
    override val bobaMainnetBscMainnetClient =
        getCrossChainClient(BobaMainnetBscMainnet(bobaMainnetExecutor, bscMainnetExecutor))
    override val bobaMainnetEthMainnetClient =
        getCrossChainClient(BobaMainnetEthMainnet(bobaMainnetExecutor, ethMainnetExecutor))
    override val polygonMainnetAvalancheMainnetClient =
        getCrossChainClient(PolygonMainnetAvalancheMainnet(polygonMainnetExecutor, avalancheMainnetExecutor))
    override val polygonMainnetEthMainnetClient =
        getCrossChainClient(PolygonMainnetEthMainnet(polygonMainnetExecutor, ethMainnetExecutor))
    override val polygonMainnetBscMainnetClient =
        getCrossChainClient(PolygonMainnetBscMainnet(polygonMainnetExecutor, bscMainnetExecutor))

    override val allCrossChainClients: List<CrossChainClient> = listOf(
        avalancheMainnetBscMainnetClient, avalancheMainnetEthMainnetClient,
        avalancheMainnetPolygonMainnetClient, ethMainnetAvalancheMainnetClient,
        ethMainnetBscMainnetClient, ethMainnetBobaMainnetClient,
        ethMainnetPolygonMainnetClient, bscMainnetAvalancheMainnetClient,
        bscMainnetPolygonMainnetClient, bscMainnetBobaMainnetClient,
        bscMainnetEthMainnetClient, bobaMainnetBscMainnetClient,
        bobaMainnetEthMainnetClient, polygonMainnetAvalancheMainnetClient,
        polygonMainnetEthMainnetClient, polygonMainnetBscMainnetClient
    )
}
