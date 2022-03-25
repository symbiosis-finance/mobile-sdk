@file:Suppress("MemberVisibilityCanBePrivate")

package com.symbiosis.sdk.network

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.ClientsManager
import com.symbiosis.sdk.contract.sortedAddresses
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.thisOrWrapped
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.gas.GasConfiguration
import com.symbiosis.sdk.network.contract.NerveContract
import com.symbiosis.sdk.network.contract.OutboundRequest
import com.symbiosis.sdk.network.contract.PoolContract
import com.symbiosis.sdk.network.contract.PortalContract
import com.symbiosis.sdk.network.contract.RouterContract
import com.symbiosis.sdk.network.contract.SynthFabricContract
import com.symbiosis.sdk.network.contract.SynthesizeContract
import com.symbiosis.sdk.network.contract.TokenContract
import com.symbiosis.sdk.network.contract.WrappedContract
import com.symbiosis.sdk.network.contract.abi.createPoolContractAbi
import com.symbiosis.sdk.network.contract.abi.createWrappedContractAbi
import com.symbiosis.sdk.network.contract.abi.fabricContractAbi
import com.symbiosis.sdk.network.contract.abi.metaRouterV2Contract
import com.symbiosis.sdk.network.contract.abi.nerveContract
import com.symbiosis.sdk.network.contract.abi.portalContractAbi
import com.symbiosis.sdk.network.contract.abi.routerContractAbi
import com.symbiosis.sdk.network.contract.abi.synthesizeContractAbi
import com.symbiosis.sdk.network.contract.metaRouter.MetaRouterV2Contract
import com.symbiosis.sdk.network.wrapper.SwapWrapper
import com.symbiosis.sdk.stuck.StuckRequest
import com.symbiosis.sdk.swap.LPTokenAddressGenerator
import com.symbiosis.sdk.swap.generate
import com.symbiosis.sdk.swap.meta.NerveStablePool
import com.symbiosis.sdk.transaction.SignedTransaction
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.BlockState
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.Web3RpcRequest
import dev.icerock.moko.web3.contract.ABIDecoder
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.contract.createErc20TokenAbi
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.requests.executeBatch
import dev.icerock.moko.web3.requests.getBlockNumber
import kotlinx.serialization.json.Json

@RequiresOptIn(
    message = "Please, consider to use ClientsManager.getNetworkClient instead of the raw constructor, because " +
            "it's signature may be changed in the future.",
    level = RequiresOptIn.Level.WARNING
)
annotation class RawUsageOfNetworkConstructor

/**
 * This is a single-network client that responds for
 * executing contracts and web3-requests.
 * In other words, wrapper around moko-web3 and symbiosis smart contracts.
 *
 * Normally created via [ClientsManager.getNetworkClient],
 * thus everything injected from sdk configuration by default.
 *
 * To get more information about contracts, use jump-to-definition and
 * check for kdoc
 */
class NetworkClient @RawUsageOfNetworkConstructor constructor(val network: Network) :
    Web3Executor by network.executor {

    // ----------------------- //
    // Smart contracts section //
    // ----------------------- //
    private val fabricSmartContract = SmartContract(
        executor = network.executor,
        contractAddress = network.synthFabricAddress,
        abiJson = fabricContractAbi
    )
    val synthFabric = SynthFabricContract(
        executor = network.executor,
        network = network,
        wrapped = fabricSmartContract
    )

    private val synthesizeSmartContract = SmartContract(
        executor = network.executor,
        contractAddress = network.synthesizeAddress,
        abiJson = synthesizeContractAbi
    )
    val synthesize = SynthesizeContract(
        executor = network.executor,
        network = network,
        wrapped = synthesizeSmartContract,
        nonceController = network.nonceController,
        tokenContractFactory = ::getTokenContract,
        defaultGasProvider = network.gasProvider
    )

    private val portalSmartContract = SmartContract(
        executor = network.executor,
        contractAddress = network.portalAddress,
        abiJson = portalContractAbi
    )
    val portal = PortalContract(
        executor = network.executor,
        network = network,
        wrapped = portalSmartContract,
        nonceController = network.nonceController,
        tokenContractFactory = ::getTokenContract,
        defaultGasProvider = network.gasProvider
    )

    private val routerSmartContract = SmartContract(
        executor = network.executor,
        contractAddress = network.routerAddress,
        abiJson = routerContractAbi
    )
    val router = RouterContract(
        executor = network.executor,
        network = network,
        wrapped = routerSmartContract,
        nonceController = network.nonceController,
        tokenContractFactory = ::getTokenContract,
        defaultSwapTTLProvider = network.swapTTLProvider,
        defaultGasProvider = network.gasProvider
    )

    private val metaRouterV2SmartContract: SmartContract = SmartContract(
        executor = network.executor,
        contractAddress = network.metaRouterAddress,
        abiJson = metaRouterV2Contract
    )
    val metaRouterV2: MetaRouterV2Contract = MetaRouterV2Contract(
        metaRouterV2SmartContract = metaRouterV2SmartContract,
        nonceController = network.nonceController,
        executor = network.executor,
        defaultGasProvider = network.gasProvider,
        tokenContractProvider = ::getTokenContract
    )

    fun getNerveContract(pool: NerveStablePool): NerveContract {
        val nerveSmartContract = SmartContract(
            executor = network.executor,
            contractAddress = pool.address,
            abiJson = nerveContract
        )
        return NerveContract(
            wrapped = nerveSmartContract,
            network = network,
            nonceController = network.nonceController,
            executor = network.executor,
            defaultGasProvider = network.gasProvider
        )
    }

    val swap = SwapWrapper(
        network = network,
        executor = network.executor,
        reservesRequestsFactory = { dex, pair ->
            getPoolContract(dex, pair).getReservesRequest()
        },
        router = router,
        defaultDexEndpoints = network.dexEndpoints
    )

    fun getTokenContract(address: ContractAddress) =
        TokenContract(
            network = network,
            executor = network.executor,
            nonceController = network.nonceController,
            wrapped = SmartContract(network.executor, address, createErc20TokenAbi(Json)),
            defaultGasProvider = network.gasProvider
        )

    fun getWrappedTokenContract(address: ContractAddress) =
        WrappedContract(
            network = network,
            executor = network.executor,
            nonceController = network.nonceController,
            wrapped = SmartContract(network.executor, address, createWrappedContractAbi(Json)),
            defaultGasProvider = network.gasProvider
        )

    fun getPoolContract(dex: DexEndpoint, pair: NetworkTokenPair): PoolContract {
        require(pair.network.chainId == network.chainId) { "You can only get PoolContract for ${network.networkName} on this NetworkClient" }

        val address = LPTokenAddressGenerator.generate(
            dex = dex,
            pair = pair
        )

        val smartContract = SmartContract(
            executor = network.executor,
            contractAddress = address,
            abiJson = createPoolContractAbi(Json)
        )

        val isReversed = listOf(pair.first.thisOrWrapped.tokenAddress, pair.second.thisOrWrapped.tokenAddress)
            .sortedAddresses()
            .first() != pair.first.thisOrWrapped.tokenAddress

        return PoolContract(
            executor = network.executor,
            wrapped = smartContract,
            isReversed = isReversed
        )
    }

    // --------------------------- //
    // Smart contracts section end //
    // --------------------------- //

    /**
     * Function for creating transfer transactions safely,
     *
     * You should consume [SignedTransaction] inside
     * the [handler] block, so nonce will be used safely
     */
    suspend fun <T> createTransferTransaction(
        credentials: Credentials,
        to: WalletAddress,
        value: BigInt,
        gasConfiguration: GasConfiguration? = null,
        handler: suspend (SignedTransaction) -> T
    ): T = network.nonceController.withNonce(credentials.address) { nonce ->
        val signedTransaction = credentials.signer.signTransferTransaction(
            nonce, network.chainId, to, value,
            gasConfiguration = gasConfiguration ?: network.gasProvider.getGasConfiguration(
                from = credentials.address,
                to = ContractAddress(to.prefixed),
                callData = null,
                value = value,
                executor = network.executor
            )
        )
        return@withNonce handler(signedTransaction)
    }

    suspend fun getStuckTransactions(
        address: WalletAddress,
        clients: List<NetworkClient>,
        blocksOffset: Int = 5_000
    ): List<StuckRequest> {
        val currentBlock = getBlockNumber()

        fun LogEvent.burnClient() = clients
            .first { burnChainId() == it.network.chainId }

        fun LogEvent.synthClient() = clients
            .first { synthChainId() == it.network.chainId }

        val (synthesizeEvents, burnEvents) = executeBatch(
            portal.getSynthesizeRequestsRequest(
                address = address,
                fromBlock = BlockState.Quantity(blockNumber = currentBlock - blocksOffset),
                toBlock = BlockState.Quantity(blockNumber = currentBlock)
            ),
            synthesize.getBurnRequestsRequest(
                address = address,
                fromBlock = BlockState.Quantity(blockNumber = currentBlock - blocksOffset),
                toBlock = BlockState.Quantity(blockNumber = currentBlock)
            )
        ).let { (synthesize, burn) ->
            synthesize.filter { log -> log.synthChainId() in clients.map { client -> client.network.chainId } } to
                    burn.filter { log -> log.burnChainId() in clients.map { client -> client.network.chainId } }
        }

        val strategies = synthesizeEvents.map { event ->
            ProcessStrategy.Synthesize(
                event.synthInternalId(),
                fromClient = this,
                targetClient = event.synthClient(),
                address
            )
        } + burnEvents.map { event ->
            ProcessStrategy.Burn(event.burnInternalId(), fromClient = this, targetClient = event.burnClient(), address)
        }

        strategies.takeIf { it.isNotEmpty() } ?: return emptyList()

        return processRequests(strategies)
    }

    private abstract class ProcessStrategy(
        val fromClient: NetworkClient,
        val targetClient: NetworkClient,
        val internalId: Hex32String
    ) {
        abstract fun getExternalId(): Hex32String
        abstract fun requestsRequest(): Web3RpcRequest<*, out OutboundRequest>
        abstract fun statesRequest(): Web3RpcRequest<*, StuckRequest.State>

        class Burn(
            internalId: Hex32String,
            fromClient: NetworkClient,
            targetClient: NetworkClient,
            private val revertableAddress: EthereumAddress
        ) : ProcessStrategy(fromClient, targetClient, internalId) {
            override fun getExternalId(): Hex32String =
                fromClient.portal.getExternalId(internalId, targetClient.network, revertableAddress)

            override fun requestsRequest() = fromClient.synthesize.requestsRequest(getExternalId())
            override fun statesRequest() = targetClient.portal.unsynthesizeStatesRequest(getExternalId())
        }

        class Synthesize(
            internalId: Hex32String,
            fromClient: NetworkClient,
            targetClient: NetworkClient,
            private val revertableAddress: EthereumAddress
        ) : ProcessStrategy(fromClient, targetClient, internalId) {
            override fun getExternalId(): Hex32String =
                fromClient.synthesize.getExternalId(internalId, targetClient.network, revertableAddress)

            override fun requestsRequest() = fromClient.portal.requestsRequest(getExternalId())
            override fun statesRequest() = targetClient.synthesize.synthesizeStatesRequest(getExternalId())
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun processRequests(strategies: List<ProcessStrategy>): List<StuckRequest> {
        val requests = executeBatch(strategies.map { it.requestsRequest() })

        val states = strategies.groupBy { it.targetClient.network.chainId }
            .entries
            .flatMap { (_, strategies) ->
                val client = strategies.first().targetClient
                client.executeBatch(strategies.map { it.statesRequest() })
            }

        return strategies
            .zip(requests)
            .zip(states) { (strategy, request), state -> Triple(strategy, request, state) }
            .filter { (_, request) -> request.state == OutboundRequest.State.Sent }
            .filter { (_, _, state) -> state == StuckRequest.State.Default }
            .map { (strategy, request, state) ->
                StuckRequest(
                    internalId = strategy.internalId,
                    externalId = strategy.getExternalId(),
                    request = request,
                    state = state,
                    fromClient = strategy.fromClient,
                    targetClient = strategy.targetClient,
                    bridgingFeeProvider = network.bridgingFeeProvider
                )
            }
    }
}

fun NetworkClient.getTokenContract(currency: Erc20Token) =
    getTokenContract(currency.tokenAddress)

// fixme: events should be decoded similar to methods
private fun LogEvent.synthChainId() = ABIDecoder
    .decodeLogEvent(portalContractAbi, event = this)
    .let { (_, _, chainId) -> chainId as BigInt }

private fun LogEvent.burnChainId() = ABIDecoder
    .decodeLogEvent(synthesizeContractAbi, event = this)
    .let { (_, _, chainId) -> chainId as BigInt }

private fun LogEvent.synthInternalId() = ABIDecoder
    .decodeLogEvent(portalContractAbi, event = this)
    .let { (internalId) -> Hex32String(internalId as ByteArray) }

private fun LogEvent.burnInternalId() = ABIDecoder
    .decodeLogEvent(synthesizeContractAbi, event = this)
    .let { (internalId) -> Hex32String(internalId as ByteArray) }
