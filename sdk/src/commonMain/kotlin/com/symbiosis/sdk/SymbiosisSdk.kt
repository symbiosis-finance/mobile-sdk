@file:Suppress("MemberVisibilityCanBePrivate")

package com.symbiosis.sdk

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.crosschain.AvalancheFujiBscTestnet
import com.symbiosis.sdk.crosschain.AvalancheFujiEthRinkeby
import com.symbiosis.sdk.crosschain.BscTestnetAvalancheFuji
import com.symbiosis.sdk.crosschain.BscTestnetEthRinkeby
import com.symbiosis.sdk.crosschain.BscTestnetPolygonMumbai
import com.symbiosis.sdk.crosschain.CrossChainClient
import com.symbiosis.sdk.crosschain.EthRinkebyAvalancheFuji
import com.symbiosis.sdk.crosschain.EthRinkebyBscTestnet
import com.symbiosis.sdk.crosschain.EthRinkebyHecoTestnet
import com.symbiosis.sdk.crosschain.EthRinkebyPolygonMumbai
import com.symbiosis.sdk.crosschain.HecoTestnetEthRinkeby
import com.symbiosis.sdk.crosschain.PolygonMumbaiBscTestnet
import com.symbiosis.sdk.crosschain.PolygonMumbaiEthRinkeby
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.networks.AvalancheFuji
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.networks.DefaultNetwork
import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.networks.HecoTestnet
import com.symbiosis.sdk.networks.PolygonMumbai
import com.symbiosis.sdk.stuck.StuckRequest
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.Web3
import dev.icerock.moko.web3.Web3Executor

open class SymbiosisSdk(
    avalancheFujiExecutor: Web3Executor,
    bscTestnetExecutor: Web3Executor,
    ethRinkebyExecutor: Web3Executor,
    hecoTestnetExecutor: Web3Executor,
    polygonMumbaiExecutor: Web3Executor
) : ClientsManager() {
    constructor(
        bscTestnetUrl: String,
        ethRinkebyUrl: String,
        polygonMumbaiUrl: String,
        avalancheFujiUrl: String = "https://api.avax-test.network/ext/bc/C/rpc",
        hecoTestnetUrl: String = "https://http-testnet.hecochain.com"
    ) : this(
        avalancheFujiExecutor = Web3(avalancheFujiUrl),
        bscTestnetExecutor = Web3(bscTestnetUrl),
        ethRinkebyExecutor = Web3(ethRinkebyUrl),
        hecoTestnetExecutor = Web3(hecoTestnetUrl),
        polygonMumbaiExecutor = Web3(polygonMumbaiUrl)
    )

    val avalancheFuji = AvalancheFuji(avalancheFujiExecutor)
    val bscTestnet = BscTestnet(bscTestnetExecutor)
    val ethRinkeby = EthRinkeby(ethRinkebyExecutor)
    val hecoTestnet = HecoTestnet(hecoTestnetExecutor)
    val polygonMumbai = PolygonMumbai(polygonMumbaiExecutor)

    val allNetworks: List<DefaultNetwork> = listOf(
        avalancheFuji, bscTestnet, ethRinkeby,
        hecoTestnet, polygonMumbai
    )
    val allTokens: List<Token> = allNetworks.flatMap(DefaultNetwork::tokens)

    val avalancheFujiClient = getNetworkClient(avalancheFuji)
    val bscTestnetClient = getNetworkClient(bscTestnet)
    val ethRinkebyClient = getNetworkClient(ethRinkeby)
    val hecoTestnetClient = getNetworkClient(hecoTestnet)
    val polygonMumbaiClient = getNetworkClient(polygonMumbai)

    val allClients: List<NetworkClient> = listOf(
        avalancheFujiClient, bscTestnetClient, ethRinkebyClient,
        hecoTestnetClient, polygonMumbaiClient
    )

    val avalancheFujiBscTestnetClient = getCrossChainClient(
        AvalancheFujiBscTestnet(avalancheFujiExecutor, bscTestnetExecutor)
    )
    val avalancheFujiEthRinkebyClient = getCrossChainClient(
        AvalancheFujiEthRinkeby(avalancheFujiExecutor, ethRinkebyExecutor)
    )
    val bscTestnetAvalancheFujiClient = getCrossChainClient(
        BscTestnetAvalancheFuji(bscTestnetExecutor, avalancheFujiExecutor)
    )
    val bscTestnetEthRinkebyClient = getCrossChainClient(
        BscTestnetEthRinkeby(bscTestnetExecutor, ethRinkebyExecutor)
    )
    val bscTestnetPolygonMumbaiClient = getCrossChainClient(
        BscTestnetPolygonMumbai(bscTestnetExecutor, polygonMumbaiExecutor)
    )
    val ethRinkebyAvalancheFujiClient = getCrossChainClient(
        EthRinkebyAvalancheFuji(ethRinkebyExecutor, avalancheFujiExecutor)
    )
    val ethRinkebyBscTestnetClient = getCrossChainClient(
        EthRinkebyBscTestnet(ethRinkebyExecutor, bscTestnetExecutor)
    )
    val ethRinkebyHecoTestnetClient = getCrossChainClient(
        EthRinkebyHecoTestnet(ethRinkebyExecutor, hecoTestnetExecutor)
    )
    val ethRinkebyPolygonMumbaiClient = getCrossChainClient(
        EthRinkebyPolygonMumbai(ethRinkebyExecutor, polygonMumbaiExecutor)
    )
    val hecoTestnetEthRinkebyClient = getCrossChainClient(
        HecoTestnetEthRinkeby(hecoTestnetExecutor, ethRinkebyExecutor)
    )
    val polygonMumbaiBscTestnetClient = getCrossChainClient(
        PolygonMumbaiBscTestnet(polygonMumbaiExecutor, bscTestnetExecutor)
    )
    val polygonMumbaiEthRinkebyClient = getCrossChainClient(
        PolygonMumbaiEthRinkeby(polygonMumbaiExecutor, ethRinkebyExecutor)
    )

    val allCrossChainClients: List<CrossChainClient> = listOf(
        avalancheFujiBscTestnetClient, avalancheFujiEthRinkebyClient,
        bscTestnetAvalancheFujiClient, bscTestnetEthRinkebyClient, bscTestnetPolygonMumbaiClient,
        ethRinkebyAvalancheFujiClient, ethRinkebyBscTestnetClient, ethRinkebyHecoTestnetClient,
        ethRinkebyPolygonMumbaiClient, hecoTestnetEthRinkebyClient,
        polygonMumbaiBscTestnetClient, polygonMumbaiEthRinkebyClient
    )

    fun getCrossChainClient(firstNetwork: Network, secondNetwork: Network) =
        getCrossChainClient(firstNetwork.chainId, secondNetwork.chainId)

    fun getCrossChainClient(firstNetworkChainId: BigInt, secondNetworkChainId: BigInt) = allCrossChainClients
        .find { it.crossChain.fromNetwork.chainId == firstNetworkChainId &&
                it.crossChain.toNetwork.chainId == secondNetworkChainId }

    suspend fun getStuckTransactions(
        address: WalletAddress,
        clients: List<NetworkClient> = allClients
    ): List<StuckRequest> = clients.flatMap { it.getStuckTransactions(address, clients) }
}
