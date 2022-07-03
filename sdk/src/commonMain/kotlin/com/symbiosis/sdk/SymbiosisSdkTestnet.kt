@file:Suppress("MemberVisibilityCanBePrivate")

package com.symbiosis.sdk

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.crosschain.testnet.AvalancheFujiBscTestnet
import com.symbiosis.sdk.crosschain.testnet.AvalancheFujiEthRinkeby
import com.symbiosis.sdk.crosschain.testnet.BobaRinkebyBscTestnet
import com.symbiosis.sdk.crosschain.testnet.BobaRinkebyEthRinkeby
import com.symbiosis.sdk.crosschain.testnet.BscTestnetAvalancheFuji
import com.symbiosis.sdk.crosschain.testnet.BscTestnetBobaRinkeby
import com.symbiosis.sdk.crosschain.testnet.BscTestnetEthRinkeby
import com.symbiosis.sdk.crosschain.testnet.BscTestnetPolygonMumbai
import com.symbiosis.sdk.crosschain.testnet.EthRinkebyAvalancheFuji
import com.symbiosis.sdk.crosschain.testnet.EthRinkebyBobaRinkeby
import com.symbiosis.sdk.crosschain.testnet.EthRinkebyBscTestnet
import com.symbiosis.sdk.crosschain.testnet.EthRinkebyPolygonMumbai
import com.symbiosis.sdk.crosschain.testnet.PolygonMumbaiBscTestnet
import com.symbiosis.sdk.crosschain.testnet.PolygonMumbaiEthRinkeby
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.networks.AvalancheFuji
import com.symbiosis.sdk.networks.BobaRinkeby
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.networks.DefaultNetwork
import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.networks.PolygonMumbai
import com.symbiosis.sdk.swap.crosschain.SymbiosisCrossChainClient
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

interface SymbiosisSdkTestnet : ClientsManager {
    val avalancheFuji: AvalancheFuji
    val bscTestnet: BscTestnet
    val ethRinkeby: EthRinkeby
    val polygonMumbai: PolygonMumbai
    val bobaRinkeby: BobaRinkeby

    override val allNetworks: List<Network>
    override val allTokens: List<DecimalsToken>

    val avalancheFujiClient: SymbiosisNetworkClient
    val bscTestnetClient: SymbiosisNetworkClient
    val ethRinkebyClient: SymbiosisNetworkClient
    val polygonMumbaiClient: SymbiosisNetworkClient
    val bobaRinkebyClient: SymbiosisNetworkClient

    override val allClients: List<SymbiosisNetworkClient>

    val avalancheFujiBscTestnetClient: SymbiosisCrossChainClient
    val avalancheFujiEthRinkebyClient: SymbiosisCrossChainClient
    val bscTestnetAvalancheFujiClient: SymbiosisCrossChainClient
    val bscTestnetEthRinkebyClient: SymbiosisCrossChainClient
    val bscTestnetPolygonMumbaiClient: SymbiosisCrossChainClient
    val bscTestnetBobaRinkebyClient: SymbiosisCrossChainClient
    val ethRinkebyAvalancheFujiClient: SymbiosisCrossChainClient
    val ethRinkebyBscTestnetClient: SymbiosisCrossChainClient
    val ethRinkebyPolygonMumbaiClient: SymbiosisCrossChainClient
    val ethRinkebyBobaRinkebyClient: SymbiosisCrossChainClient
    val polygonMumbaiBscTestnetClient: SymbiosisCrossChainClient
    val polygonMumbaiEthRinkebyClient: SymbiosisCrossChainClient
    val bobaRinkebyEthRinkebyClient: SymbiosisCrossChainClient
    val bobaRinkebyBscTestnetClient: SymbiosisCrossChainClient

    override val allCrossChainClients: List<SymbiosisCrossChainClient>
}

fun SymbiosisSdkTestnet(
    bscTestnetUrl: String,
    ethRinkebyUrl: String,
    polygonMumbaiUrl: String,
    avalancheFujiUrl: String = "https://api.avax-test.network/ext/bc/C/rpc",
    bobaRinkebyUrl: String = "https://rinkeby.boba.network/",
    web3Provider: (chainId: BigInt, url: String) -> Web3Executor = ::Web3
) = SymbiosisSdkTestnet(
    avalancheFujiExecutor = web3Provider(AvalancheFuji.CHAIN_ID, avalancheFujiUrl),
    bscTestnetExecutor = web3Provider(BscTestnet.CHAIN_ID, bscTestnetUrl),
    ethRinkebyExecutor = web3Provider(EthRinkeby.CHAIN_ID, ethRinkebyUrl),
    polygonMumbaiExecutor = web3Provider(PolygonMumbai.CHAIN_ID, polygonMumbaiUrl),
    bobaRinkebyExecutor = web3Provider(BobaRinkeby.CHAIN_ID, bobaRinkebyUrl)
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
    override val allTokens: List<DecimalsToken> = allNetworks.flatMap(DefaultNetwork::tokens)

    override val avalancheFujiClient = avalancheFuji.symbiosisClient
    override val bscTestnetClient = bscTestnet.symbiosisClient
    override val ethRinkebyClient = ethRinkeby.symbiosisClient
    override val polygonMumbaiClient = polygonMumbai.symbiosisClient
    override val bobaRinkebyClient = bobaRinkeby.symbiosisClient

    override val allClients: List<SymbiosisNetworkClient> = listOf(
        avalancheFujiClient, bscTestnetClient,
        ethRinkebyClient, polygonMumbaiClient,
        bobaRinkebyClient
    )

    override val avalancheFujiBscTestnetClient =
        SymbiosisCrossChainClient(AvalancheFujiBscTestnet(avalancheFujiExecutor, bscTestnetExecutor))
    override val avalancheFujiEthRinkebyClient =
        SymbiosisCrossChainClient(AvalancheFujiEthRinkeby(avalancheFujiExecutor, ethRinkebyExecutor))
    override val bscTestnetAvalancheFujiClient =
        SymbiosisCrossChainClient(BscTestnetAvalancheFuji(bscTestnetExecutor, avalancheFujiExecutor))
    override val bscTestnetEthRinkebyClient =
        SymbiosisCrossChainClient(BscTestnetEthRinkeby(bscTestnetExecutor, ethRinkebyExecutor))
    override val bscTestnetPolygonMumbaiClient =
        SymbiosisCrossChainClient(BscTestnetPolygonMumbai(bscTestnetExecutor, polygonMumbaiExecutor))
    override val bscTestnetBobaRinkebyClient =
        SymbiosisCrossChainClient(BscTestnetBobaRinkeby(bscTestnetExecutor, bobaRinkebyExecutor))
    override val ethRinkebyAvalancheFujiClient =
        SymbiosisCrossChainClient(EthRinkebyAvalancheFuji(ethRinkebyExecutor, avalancheFujiExecutor))
    override val ethRinkebyBscTestnetClient =
        SymbiosisCrossChainClient(EthRinkebyBscTestnet(ethRinkebyExecutor, bscTestnetExecutor))
    override val ethRinkebyPolygonMumbaiClient =
        SymbiosisCrossChainClient(EthRinkebyPolygonMumbai(ethRinkebyExecutor, polygonMumbaiExecutor))
    override val ethRinkebyBobaRinkebyClient =
        SymbiosisCrossChainClient(EthRinkebyBobaRinkeby(ethRinkebyExecutor, bobaRinkebyExecutor))
    override val polygonMumbaiBscTestnetClient =
        SymbiosisCrossChainClient(PolygonMumbaiBscTestnet(polygonMumbaiExecutor, bscTestnetExecutor))
    override val polygonMumbaiEthRinkebyClient =
        SymbiosisCrossChainClient(PolygonMumbaiEthRinkeby(polygonMumbaiExecutor, ethRinkebyExecutor))
    override val bobaRinkebyEthRinkebyClient =
        SymbiosisCrossChainClient(BobaRinkebyEthRinkeby(bobaRinkebyExecutor, ethRinkebyExecutor))
    override val bobaRinkebyBscTestnetClient =
        SymbiosisCrossChainClient(BobaRinkebyBscTestnet(bobaRinkebyExecutor, bscTestnetExecutor))

    override val allCrossChainClients: List<SymbiosisCrossChainClient> = listOf(
        avalancheFujiBscTestnetClient, avalancheFujiEthRinkebyClient,
        bscTestnetAvalancheFujiClient, bscTestnetEthRinkebyClient,
        bscTestnetPolygonMumbaiClient, ethRinkebyAvalancheFujiClient,
        ethRinkebyBscTestnetClient, ethRinkebyPolygonMumbaiClient,
        polygonMumbaiBscTestnetClient, polygonMumbaiEthRinkebyClient,
        bscTestnetBobaRinkebyClient, ethRinkebyBobaRinkebyClient,
        bobaRinkebyEthRinkebyClient, bobaRinkebyBscTestnetClient
    )
}

