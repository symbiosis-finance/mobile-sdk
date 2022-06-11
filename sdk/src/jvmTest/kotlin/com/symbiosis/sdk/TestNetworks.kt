package com.symbiosis.sdk

import com.symbiosis.sdk.networks.AvalancheFuji
import com.symbiosis.sdk.networks.BobaRinkeby
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.networks.PolygonMumbai
import dev.icerock.moko.web3.Web3
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

fun testWeb3(endpointUrl: String) = Web3(
    httpClient = HttpClient {
        install(Logging) {
            level = LogLevel.ALL
        }
        install(DefaultRequest) {
            contentType(ContentType.Application.Json)
        }
    },
    json = Json {
        ignoreUnknownKeys = true
    },
    endpointUrl = endpointUrl
)

val testETH = EthRinkeby(testWeb3("https://rinkeby.infura.io/v3/f5439191641b4c21885e635b5138e08c"))
val testBSC = BscTestnet(testWeb3("https://bsc.getblock.io/testnet/?api_key=b5945b69-c6c6-41c7-8541-568cac117dfe"))
val testMumbai =
    PolygonMumbai(testWeb3("https://matic.getblock.io/testnet/?api_key=b5945b69-c6c6-41c7-8541-568cac117dfe"))
val testAvalanche = AvalancheFuji(testWeb3("https://api.avax-test.network/ext/bc/C/rpc"))
val testBoba = BobaRinkeby(testWeb3("https://rinkeby.boba.network"))

val testSdk = SymbiosisSdkTestnet(
    avalancheFujiExecutor = testAvalanche.executor,
    bscTestnetExecutor = testBSC.executor,
    ethRinkebyExecutor = testETH.executor,
    bobaRinkebyExecutor = testBoba.executor,
    polygonMumbaiExecutor = testMumbai.executor
)

val mainnetSdk = SymbiosisSdkMainnet(
    ethMainnetUrl = "https://rpc.symbiosis.finance/1",
    bscMainnetUrl = "https://rpc.symbiosis.finance/56",
    polygonMainnetUrl = "https://rpc.symbiosis.finance/137"
)
