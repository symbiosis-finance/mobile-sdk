package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.contract.WriteRequest
import com.symbiosis.sdk.contract.write
import com.symbiosis.sdk.contract.writeRequest
import com.symbiosis.sdk.internal.nonce.NonceController
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.stuck.StuckRequest
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.BlockState
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.crypto.KeccakParameter
import dev.icerock.moko.web3.crypto.digestKeccak
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.hex.fillToHex32
import dev.icerock.moko.web3.requests.executeBatch

class PortalContract internal constructor(
    private val executor: Web3Executor,
    private val network: Network,
    private val wrapped: SmartContract,
    private val nonceController: NonceController,
    private val tokenContractFactory: (ContractAddress) -> TokenContract,
    private val defaultGasProvider: GasProvider,
) {
    val address: ContractAddress = wrapped.contractAddress

    suspend fun synthesize(
        credentials: Credentials,
        stableBridgingFee: BigInt,
        amount: BigInt,
        realCurrencyAddress: ContractAddress,
        targetNetwork: Network,
        gasProvider: GasProvider? = null
    ): TransactionHash {
        tokenContractFactory(realCurrencyAddress)
            .approveMaxIfNeed(credentials, wrapped.contractAddress, amount, gasProvider)

        return wrapped.write(
            chainId = network.chainId,
            nonceController = nonceController,
            credentials = credentials,
            method = "synthesize",
            params = listOf(
                stableBridgingFee,
                realCurrencyAddress.bigInt,
                amount,
                credentials.address.bigInt,
                targetNetwork.synthesizeAddress.bigInt,
                targetNetwork.bridgeAddress.bigInt,
                credentials.address.bigInt,
                targetNetwork.chainId
            ),
            gasProvider = gasProvider ?: defaultGasProvider,
            value = 10_000_000_000_000_000.bi
        )
    }

    fun getMetaSynthesizeCalldata(
        stableBridgingFee: BigInt,
        amount: BigInt,
        rtoken: ContractAddress,
        chain2address: EthereumAddress,
        receiveSide: ContractAddress,
        oppositeBridge: ContractAddress,
        fromAddress: EthereumAddress,
        chainId: BigInt,
        swapTokens: List<ContractAddress>,
        secondDexRouter: ContractAddress,
        secondSwapCalldata: HexString,
        finalDexRouter: ContractAddress,
        finalSwapCalldata: HexString?,
        finalOffset: BigInt
    ): HexString = wrapped.encodeMethod(
        method = "metaSynthesize",
        params = listOf(
            listOf(
                stableBridgingFee,
                amount,
                rtoken.bigInt,
                chain2address.bigInt,
                receiveSide.bigInt,
                oppositeBridge.bigInt,
                fromAddress.bigInt,
                chainId,
                swapTokens.map(ContractAddress::bigInt),
                secondDexRouter.bigInt,
                secondSwapCalldata.byteArray,
                finalDexRouter.bigInt,
                finalSwapCalldata?.byteArray ?: byteArrayOf(),
                finalOffset,
                chain2address.bigInt
            )
        )
    )

    fun getMetaUnsynthesizeCalldata(
        token: ContractAddress,
        amount: BigInt,
        to: EthereumAddress,
        synthesisRequestsCount: BigInt,
        finalNetwork: Network,
        finalSwapCalldata: HexString?,
        finalOffset: BigInt
    ): HexString = wrapped.encodeMethod(
        method = "metaUnsynthesize",
        params = listOf(
            1.bi, // bridging fee
            getExternalId(synthesisRequestsCount, finalNetwork, to).byteArray, // tx id
            to.bigInt, // toAddress
            amount,
            token.bigInt,
            finalNetwork.routerAddress.bigInt,
            finalSwapCalldata?.byteArray ?: byteArrayOf(),
            finalOffset
        )
    )

    fun revertBurnRequestRequest(
        stableBridgingFee: BigInt,
        internalId: Hex32String,
        receiveSide: ContractAddress,
        oppositeBridge: ContractAddress,
        chainIdFrom: BigInt
    ): WriteRequest = wrapped.writeRequest(
        method = "revertBurnRequest",
        params = listOf(stableBridgingFee, internalId.byteArray, receiveSide.bigInt, oppositeBridge.bigInt, chainIdFrom)
    )

    suspend fun revertBurnRequest(
        credentials: Credentials,
        stableBridgingFee: BigInt,
        internalId: Hex32String,
        receiveSide: ContractAddress,
        oppositeBridge: ContractAddress,
        chainIdFrom: BigInt,
        gasProvider: GasProvider = defaultGasProvider,
    ) = revertBurnRequestRequest(stableBridgingFee, internalId, receiveSide, oppositeBridge, chainIdFrom)
        .write(credentials, network.chainId, gasProvider, nonceController)

    fun revertSynthesizeRequest(
        stableBridgingFee: BigInt,
        externalId: Hex32String
    ) = wrapped.writeRequest(
        method = "revertSynthesize",
        params = listOf(stableBridgingFee, externalId.byteArray)
    )

    data class SynthOutboundRequest(
        override val recipient: EthereumAddress,
        override val chain2address: WalletAddress,
        override val amount: BigInt,
        val rtokenAddress: ContractAddress,
        override val state: OutboundRequest.State
    ) : OutboundRequest {
        override val tokenAddress: ContractAddress = rtokenAddress
    }

    fun requestsRequest(externalId: Hex32String) = wrapped.readRequest(
        method = "requests",
        params = listOf(externalId.byteArray),
        mapper = { (recipient, chain2address, amount, rtoken, state) ->
            SynthOutboundRequest(
                EthereumAddress.createInstance(recipient as BigInt),
                WalletAddress.createInstance(chain2address as BigInt),
                amount as BigInt,
                ContractAddress.createInstance(rtoken as BigInt),
                OutboundRequest.State.values().first { it.ordinal == (state as BigInt).toInt() }
            )
        }
    )

    fun unsynthesizeStatesRequest(externalId: Hex32String) = wrapped.readRequest(
        method = "unsynthesizeStates",
        params = listOf(externalId.byteArray),
        mapper = { (state) -> StuckRequest.State.values().first { it.ordinal == (state as BigInt).toInt() } }
    )

    fun getExternalId(
        synthesisRequestsCount: BigInt,
        finalNetwork: Network,
        revertableAddress: EthereumAddress
    ): Hex32String {
        val internalIdData = network.synthesizeAddress.byteArray +
                HexString(synthesisRequestsCount).fillToHex32().byteArray +
                HexString(network.chainId).fillToHex32().byteArray

        return internalIdData.digestKeccak(KeccakParameter.KECCAK_256)
            .let(::Hex32String)
            .let { getExternalId(it, finalNetwork, revertableAddress) }
    }

    fun getExternalId(
        internalId: Hex32String,
        finalNetwork: Network,
        revertableAddress: EthereumAddress
    ): Hex32String {
        val externalIdData = internalId.byteArray +
                finalNetwork.portalAddress.byteArray +
                revertableAddress.byteArray +
                HexString(finalNetwork.chainId).fillToHex32().byteArray

        return externalIdData
            .digestKeccak(KeccakParameter.KECCAK_256)
            .let(::Hex32String)
    }

    suspend fun requestsCount(): BigInt = executor.executeBatch(requestsCountRequest()).first()

    fun requestsCountRequest() =
        wrapped.readRequest(
            method = "requestCount",
            params = listOf()
        ) { (count) -> count as BigInt }

    fun burnCompletedEventFilter(
        requestId: Hex32String,
        finalNetwork: Network,
        revertableAddress: EthereumAddress
    ): List<Hex32String> = listOf(
        wrapped.hashEventSignature(event = "BurnCompleted"),
        getExternalId(requestId, finalNetwork, revertableAddress)
    )

    fun synthesizeRequestEventFilter(
        address: WalletAddress,
        otherChainId: BigInt? = null
    ): List<Hex32String?> = listOf(
        wrapped.hashEventSignature(event = "SynthesizeRequest"),
        address.fillToHex32(),
        otherChainId?.let(::Hex32String)
    )

    fun getSynthesizeRequestsRequest(
        address: WalletAddress,
        otherChainId: BigInt? = null,
        fromBlock: BlockState? = null,
        toBlock: BlockState? = null
    ) = wrapped.getLogsRequest(fromBlock, toBlock, topics = synthesizeRequestEventFilter(address, otherChainId))

    suspend fun getSynthesizeRequests(
        address: WalletAddress,
        otherChainId: BigInt? = null,
        fromBlock: BlockState? = null,
        toBlock: BlockState? = null
    ): List<LogEvent> = executor
        .executeBatch(getSynthesizeRequestsRequest(address, otherChainId, fromBlock, toBlock))
        .first()
}
