package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.stuck.StuckTransaction
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.contract.WriteRequest
import dev.icerock.moko.web3.crypto.KeccakParameter
import dev.icerock.moko.web3.crypto.digestKeccak
import dev.icerock.moko.web3.entity.BlockState
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.entity.TransactionHash
import dev.icerock.moko.web3.entity.WalletAddress
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.hex.HexString
import dev.icerock.moko.web3.hex.fillToHex32
import dev.icerock.moko.web3.requests.executeBatch
import dev.icerock.moko.web3.signing.Credentials

class PortalContract internal constructor(
    private val executor: Web3Executor,
    private val network: Network,
    private val wrapped: SmartContract,
    private val tokenContractFactory: (ContractAddress) -> TokenContract,
) {
    val address: ContractAddress = wrapped.contractAddress

    suspend fun synthesize(
        credentials: Credentials,
        stableBridgingFee: BigInt,
        amount: BigInt,
        realCurrencyAddress: ContractAddress,
        targetNetwork: Network,
    ): TransactionHash {
        tokenContractFactory(realCurrencyAddress)
            .approveMaxIfNeed(credentials, wrapped.contractAddress, amount)

        return wrapped.write(
            credentials = credentials,
            method = "synthesize",
            params = listOf(
                stableBridgingFee,
                realCurrencyAddress,
                amount,
                credentials.address,
                targetNetwork.synthesizeAddress,
                targetNetwork.bridgeAddress,
                credentials.address,
                targetNetwork.chainId,
                ClientId
            ),
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
    ): HexString = wrapped.writeRequest(
        method = "metaSynthesize",
        params = listOf(
            listOf(
                stableBridgingFee,
                amount,
                rtoken,
                chain2address,
                receiveSide,
                oppositeBridge,
                fromAddress,
                chainId,
                swapTokens,
                secondDexRouter,
                secondSwapCalldata.byteArray,
                finalDexRouter,
                finalSwapCalldata?.byteArray ?: byteArrayOf(),
                finalOffset,
                chain2address,
                ClientId
            )
        )
    ).callData

    fun getMetaUnsynthesizeCalldata(
        token: ContractAddress,
        amount: BigInt,
        to: EthereumAddress,
        synthesisRequestsCount: BigInt,
        finalDexAddress: ContractAddress,
        finalNetwork: Network,
        finalSwapCalldata: HexString?,
        finalOffset: BigInt
    ): HexString = wrapped.writeRequest(
        method = "metaUnsynthesize",
        params = listOf(
            1.bi, // bridging fee
            getExternalId(synthesisRequestsCount, finalNetwork, to).byteArray, // tx id
            to, // toAddress
            amount,
            token,
            finalDexAddress,
            finalSwapCalldata?.byteArray ?: byteArrayOf(),
            finalOffset
        )
    ).callData

    fun revertBurnRequestRequest(
        stableBridgingFee: BigInt,
        internalId: Hex32String,
        receiveSide: ContractAddress,
        oppositeBridge: ContractAddress,
        chainIdFrom: BigInt
    ): WriteRequest = wrapped.writeRequest(
        method = "revertBurnRequest",
        params = listOf(
            stableBridgingFee,
            internalId.byteArray,
            receiveSide,
            oppositeBridge,
            chainIdFrom,
            ClientId
        )
    )

    suspend fun revertBurnRequest(
        credentials: Credentials,
        stableBridgingFee: BigInt,
        internalId: Hex32String,
        receiveSide: ContractAddress,
        oppositeBridge: ContractAddress,
        chainIdFrom: BigInt
    ) = revertBurnRequestRequest(stableBridgingFee, internalId, receiveSide, oppositeBridge, chainIdFrom)
        .send(credentials)

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
                recipient as EthereumAddress,
                (chain2address as EthereumAddress).run { WalletAddress(prefixed) },
                amount as BigInt,
                (rtoken as EthereumAddress).run { ContractAddress(prefixed) },
                OutboundRequest.State.values().first { it.ordinal == (state as BigInt).toInt() }
            )
        }
    )

    fun unsynthesizeStatesRequest(externalId: Hex32String) = wrapped.readRequest(
        method = "unsynthesizeStates",
        params = listOf(externalId.byteArray),
        mapper = { (state) -> StuckTransaction.State.values().first { it.ordinal == (state as BigInt).toInt() } }
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
