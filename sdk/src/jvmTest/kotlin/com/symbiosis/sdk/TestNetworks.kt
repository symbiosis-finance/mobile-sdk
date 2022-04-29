package com.symbiosis.sdk

import com.symbiosis.sdk.networks.AvalancheFuji
import com.symbiosis.sdk.networks.BscTestnet
import com.symbiosis.sdk.networks.EthRinkeby
import com.symbiosis.sdk.networks.HecoTestnet
import com.symbiosis.sdk.networks.PolygonMumbai
import dev.icerock.moko.web3.Web3
import io.ktor.client.HttpClient
import io.ktor.client.features.DefaultRequest
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
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
    json = Json,
    endpointUrl = endpointUrl
)

val testETH = EthRinkeby(endpointUrl = "https://rinkeby.infura.io/v3/f5439191641b4c21885e635b5138e08c")
val testBSC = BscTestnet(endpointUrl = "https://bsc.getblock.io/testnet/?api_key=b5945b69-c6c6-41c7-8541-568cac117dfe")
val testMumbai =
    PolygonMumbai(endpointUrl = "https://matic.getblock.io/testnet/?api_key=b5945b69-c6c6-41c7-8541-568cac117dfe")
val testAvalanche = AvalancheFuji(endpointUrl = "https://api.avax-test.network/ext/bc/C/rpc")
val testHeco = HecoTestnet(endpointUrl = "https://http-testnet.hecochain.com")

val testSdk = SymbiosisSdkTestnet(
    avalancheFujiExecutor = testAvalanche.executor,
    bscTestnetExecutor = testBSC.executor,
    ethRinkebyExecutor = testETH.executor,
    hecoTestnetExecutor = testHeco.executor,
    polygonMumbaiExecutor = testMumbai.executor
)