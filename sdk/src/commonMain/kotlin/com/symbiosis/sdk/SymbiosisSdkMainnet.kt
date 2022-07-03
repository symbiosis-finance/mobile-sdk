package com.symbiosis.sdk

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.crosschain.mainnet.AvalancheMainnetBscMainnet
import com.symbiosis.sdk.crosschain.mainnet.AvalancheMainnetEthMainnet
import com.symbiosis.sdk.crosschain.mainnet.AvalancheMainnetPolygonMainnet
import com.symbiosis.sdk.crosschain.mainnet.BobaMainnetBscMainnet
import com.symbiosis.sdk.crosschain.mainnet.BobaMainnetEthMainnet
import com.symbiosis.sdk.crosschain.mainnet.BscMainnetAvalancheMainnet
import com.symbiosis.sdk.crosschain.mainnet.BscMainnetBobaMainnet
import com.symbiosis.sdk.crosschain.mainnet.BscMainnetEthMainnet
import com.symbiosis.sdk.crosschain.mainnet.BscMainnetPolygonMainnet
import com.symbiosis.sdk.crosschain.mainnet.EthMainnetAvalancheMainnet
import com.symbiosis.sdk.crosschain.mainnet.EthMainnetBobaMainnet
import com.symbiosis.sdk.crosschain.mainnet.EthMainnetBscMainnet
import com.symbiosis.sdk.crosschain.mainnet.EthMainnetPolygonMainnet
import com.symbiosis.sdk.crosschain.mainnet.PolygonMainnetAvalancheMainnet
import com.symbiosis.sdk.crosschain.mainnet.PolygonMainnetBscMainnet
import com.symbiosis.sdk.crosschain.mainnet.PolygonMainnetEthMainnet
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.networks.AvalancheMainnet
import com.symbiosis.sdk.networks.BobaMainnet
import com.symbiosis.sdk.networks.BscMainnet
import com.symbiosis.sdk.networks.DefaultNetwork
import com.symbiosis.sdk.networks.EthMainnet
import com.symbiosis.sdk.networks.PolygonMainnet
import com.symbiosis.sdk.swap.crosschain.SymbiosisCrossChainClient
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

interface SymbiosisSdkMainnet : ClientsManager {
    val avalancheMainnet: AvalancheMainnet
    val bscMainnet: BscMainnet
    val ethMainnet: EthMainnet
    val polygonMainnet: PolygonMainnet
    val bobaMainnet: BobaMainnet

    override val allNetworks: List<Network>
    override val allTokens: List<DecimalsToken>

    val avalancheMainnetClient: SymbiosisNetworkClient
    val bscMainnetClient: SymbiosisNetworkClient
    val ethMainnetClient: SymbiosisNetworkClient
    val polygonMainnetClient: SymbiosisNetworkClient
    val bobaMainnetClient: SymbiosisNetworkClient

    override val allClients: List<SymbiosisNetworkClient>

    val avalancheMainnetEthMainnetClient: SymbiosisCrossChainClient
    val avalancheMainnetBscMainnetClient: SymbiosisCrossChainClient
    val avalancheMainnetPolygonMainnetClient: SymbiosisCrossChainClient
    val ethMainnetAvalancheMainnetClient: SymbiosisCrossChainClient
    val ethMainnetBscMainnetClient: SymbiosisCrossChainClient
    val ethMainnetBobaMainnetClient: SymbiosisCrossChainClient
    val ethMainnetPolygonMainnetClient: SymbiosisCrossChainClient
    val bscMainnetAvalancheMainnetClient: SymbiosisCrossChainClient
    val bscMainnetPolygonMainnetClient: SymbiosisCrossChainClient
    val bscMainnetBobaMainnetClient: SymbiosisCrossChainClient
    val bscMainnetEthMainnetClient: SymbiosisCrossChainClient
    val bobaMainnetBscMainnetClient: SymbiosisCrossChainClient
    val bobaMainnetEthMainnetClient: SymbiosisCrossChainClient
    val polygonMainnetAvalancheMainnetClient: SymbiosisCrossChainClient
    val polygonMainnetEthMainnetClient: SymbiosisCrossChainClient
    val polygonMainnetBscMainnetClient: SymbiosisCrossChainClient

    override val allCrossChainClients: List<SymbiosisCrossChainClient>
}

fun SymbiosisSdkMainnet(
    bscMainnetUrl: String,
    ethMainnetUrl: String,
    polygonMainnetUrl: String = "https://polygon-rpc.com",
    avalancheMainnetUrl: String = "https://api.avax.network/ext/bc/C/rpc",
    bobaMainnetUrl: String = "https://mainnet.boba.network/",
    web3Provider: (chainId: BigInt, url: String) -> Web3Executor = ::Web3
) = SymbiosisSdkMainnet(
    avalancheMainnetExecutor = web3Provider(AvalancheMainnet.CHAIN_ID, avalancheMainnetUrl),
    bscMainnetExecutor = web3Provider(BscMainnet.CHAIN_ID, bscMainnetUrl),
    ethMainnetExecutor = web3Provider(EthMainnet.CHAIN_ID, ethMainnetUrl),
    polygonMainnetExecutor = web3Provider(PolygonMainnet.CHAIN_ID, polygonMainnetUrl),
    bobaMainnetExecutor = web3Provider(BobaMainnet.CHAIN_ID, bobaMainnetUrl)
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
    override val allTokens: List<DecimalsToken> = allNetworks.flatMap(DefaultNetwork::tokens)

    override val avalancheMainnetClient = avalancheMainnet.symbiosisClient
    override val bscMainnetClient: SymbiosisNetworkClient = bscMainnet.symbiosisClient
    override val ethMainnetClient: SymbiosisNetworkClient = ethMainnet.symbiosisClient
    override val polygonMainnetClient: SymbiosisNetworkClient = polygonMainnet.symbiosisClient
    override val bobaMainnetClient: SymbiosisNetworkClient = bobaMainnet.symbiosisClient

    override val allClients: List<SymbiosisNetworkClient> = listOf(
        avalancheMainnetClient, bscMainnetClient,
        ethMainnetClient, polygonMainnetClient,
        bobaMainnetClient
    )

    override val avalancheMainnetEthMainnetClient =
        SymbiosisCrossChainClient(AvalancheMainnetEthMainnet(avalancheMainnetExecutor, ethMainnetExecutor))
    override val avalancheMainnetBscMainnetClient =
        SymbiosisCrossChainClient(AvalancheMainnetBscMainnet(avalancheMainnetExecutor, bscMainnetExecutor))
    override val avalancheMainnetPolygonMainnetClient =
        SymbiosisCrossChainClient(AvalancheMainnetPolygonMainnet(avalancheMainnetExecutor, polygonMainnetExecutor))
    override val ethMainnetAvalancheMainnetClient =
        SymbiosisCrossChainClient(EthMainnetAvalancheMainnet(ethMainnetExecutor, avalancheMainnetExecutor))
    override val ethMainnetBscMainnetClient =
        SymbiosisCrossChainClient(EthMainnetBscMainnet(ethMainnetExecutor, bscMainnetExecutor))
    override val ethMainnetBobaMainnetClient =
        SymbiosisCrossChainClient(EthMainnetBobaMainnet(ethMainnetExecutor, bobaMainnetExecutor))
    override val ethMainnetPolygonMainnetClient =
        SymbiosisCrossChainClient(EthMainnetPolygonMainnet(ethMainnetExecutor, polygonMainnetExecutor))
    override val bscMainnetAvalancheMainnetClient =
        SymbiosisCrossChainClient(BscMainnetAvalancheMainnet(bscMainnetExecutor, avalancheMainnetExecutor))
    override val bscMainnetPolygonMainnetClient =
        SymbiosisCrossChainClient(BscMainnetPolygonMainnet(bscMainnetExecutor, polygonMainnetExecutor))
    override val bscMainnetBobaMainnetClient =
        SymbiosisCrossChainClient(BscMainnetBobaMainnet(bscMainnetExecutor, bobaMainnetExecutor))
    override val bscMainnetEthMainnetClient =
        SymbiosisCrossChainClient(BscMainnetEthMainnet(bscMainnetExecutor, ethMainnetExecutor))
    override val bobaMainnetBscMainnetClient =
        SymbiosisCrossChainClient(BobaMainnetBscMainnet(bobaMainnetExecutor, bscMainnetExecutor))
    override val bobaMainnetEthMainnetClient =
        SymbiosisCrossChainClient(BobaMainnetEthMainnet(bobaMainnetExecutor, ethMainnetExecutor))
    override val polygonMainnetAvalancheMainnetClient =
        SymbiosisCrossChainClient(PolygonMainnetAvalancheMainnet(polygonMainnetExecutor, avalancheMainnetExecutor))
    override val polygonMainnetEthMainnetClient =
        SymbiosisCrossChainClient(PolygonMainnetEthMainnet(polygonMainnetExecutor, ethMainnetExecutor))
    override val polygonMainnetBscMainnetClient =
        SymbiosisCrossChainClient(PolygonMainnetBscMainnet(polygonMainnetExecutor, bscMainnetExecutor))

    override val allCrossChainClients: List<SymbiosisCrossChainClient> = listOf(
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
