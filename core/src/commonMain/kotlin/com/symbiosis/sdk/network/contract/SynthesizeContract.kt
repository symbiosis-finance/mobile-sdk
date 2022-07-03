@file:OptIn(ExperimentalStdlibApi::class)

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


class SynthesizeContract internal constructor(
    private val executor: Web3Executor,
    private val network: Network,
    private val wrapped: SmartContract,
    private val tokenContractFactory: (ContractAddress) -> TokenContract,
) {
    val address: ContractAddress = wrapped.contractAddress
    suspend fun burnSynthTokens(
        credentials: Credentials,
        stableBridgingFee: BigInt,
        amount: BigInt,
        synthCurrencyAddress: ContractAddress,
        targetNetwork: Network
    ): TransactionHash {
        tokenContractFactory(synthCurrencyAddress)
            .approveMaxIfNeed(credentials, wrapped.contractAddress, amount)

        return wrapped.write(
            credentials = credentials,
            method = "burnSyntheticToken",
            params = listOf(
                stableBridgingFee,
                synthCurrencyAddress,
                amount,
                credentials.address,
                targetNetwork.portalAddress,
                targetNetwork.bridgeAddress,
                credentials.address,
                targetNetwork.chainId,
                ClientId
            ),
            value = 10_000_000_000_000_000.bi
        )
    }

    fun getMetaBurnSynthTokenCalldata(
        stableBridgingFee: BigInt,
        amount: BigInt,
        fromAddress: EthereumAddress,
        finalDexRouter: ContractAddress,
        sToken: ContractAddress,
        finalSwapCallData: HexString?,
        finalOffset: BigInt?,
        chain2Address: EthereumAddress,
        receiveSide: ContractAddress,
        oppositeBridge: ContractAddress,
        chainId: BigInt
    ): HexString = wrapped.writeRequest(
        method = "metaBurnSyntheticToken",
        params = listOf(
            listOf(
                stableBridgingFee,
                amount,
                fromAddress,
                finalDexRouter,
                sToken,
                finalSwapCallData?.byteArray ?: byteArrayOf(),
                finalOffset ?: 0.bi,
                chain2Address,
                receiveSide,
                oppositeBridge,
                chain2Address,
                chainId,
                ClientId
            )
        )
    ).callData

    fun getMetaMintSyntheticTokenCalldata(
        to: EthereumAddress,
        portalRequestsCount: BigInt,
        finalNetwork: Network,
        finalDexRouter: ContractAddress,
        finalSwapCallData: HexString?,
        finalSwapOffset: BigInt,
        swapTokens: List<ContractAddress>,
        stableSwapCallData: HexString,
        stablePoolAddress: ContractAddress,
        firstSwapAmountOut: BigInt,
        firstStableToken: ContractAddress,
    ): HexString = wrapped.writeRequest(
        method = "metaMintSyntheticToken",
        params = listOf(
            listOf(
                1.bi, // bridging fee
                firstSwapAmountOut, // amount
                getExternalId(portalRequestsCount, finalNetwork, to).byteArray, // externalId
                firstStableToken, // rtoken
                network.chainId, // chainId
                to,
                swapTokens, // swap tokens
                stablePoolAddress, // second dex router
                stableSwapCallData.byteArray, // second swap call data
                finalDexRouter,
                finalSwapCallData?.byteArray ?: byteArrayOf(),
                finalSwapOffset
            )
        )
    ).callData

    fun revertSynthesizeRequestRequest(
        stableBridgingFee: BigInt,
        internalId: Hex32String,
        receiveSide: ContractAddress,
        oppositeBridge: ContractAddress,
        chainIdFrom: BigInt
    ): WriteRequest = wrapped.writeRequest(
        method = "revertSynthesizeRequest",
        params = listOf(
            stableBridgingFee,
            internalId.byteArray,
            receiveSide,
            oppositeBridge,
            chainIdFrom,
            ClientId
        )
    )

    suspend fun revertSynthesizeRequest(
        credentials: Credentials,
        stableBridgingFee: BigInt,
        internalId: Hex32String,
        receiveSide: ContractAddress,
        oppositeBridge: ContractAddress,
        chainIdFrom: BigInt
    ) = revertSynthesizeRequestRequest(stableBridgingFee, internalId, receiveSide, oppositeBridge, chainIdFrom)
        .send(credentials)

    fun revertBurnRequest(
        stableBridgingFee: BigInt,
        externalId: Hex32String
    ): WriteRequest = wrapped.writeRequest(
        method = "revertBurn",
        params = listOf(stableBridgingFee, externalId.byteArray)
    )

    data class BurnOutboundRequest(
        override val recipient: EthereumAddress,
        override val chain2address: WalletAddress,
        override val amount: BigInt,
        override val tokenAddress: ContractAddress,
        val sTokenAddress: ContractAddress,
        override val state: OutboundRequest.State
    ) : OutboundRequest

    fun requestsRequest(externalId: Hex32String) = wrapped.readRequest(
        method = "requests",
        params = listOf(externalId.byteArray),
        mapper = { (recipient, chain2address, amount, token, stoken, state) ->
            BurnOutboundRequest(
                recipient = recipient as EthereumAddress,
                chain2address = (chain2address as EthereumAddress).run { WalletAddress(prefixed) },
                amount = amount as BigInt,
                tokenAddress = (token as EthereumAddress).run { ContractAddress(prefixed) },
                sTokenAddress = (stoken as EthereumAddress).run { ContractAddress(prefixed) },
                state = OutboundRequest.State.values().first { it.ordinal == (state as BigInt).toInt() }
            )
        }
    )

    private operator fun <T> List<T>.component6() = this[5]

    fun synthesizeStatesRequest(externalId: Hex32String) = wrapped.readRequest(
        method = "synthesizeStates",
        params = listOf(externalId.byteArray),
        mapper = { (state) ->
            state as BigInt
            StuckTransaction.State.values().first { it.value == state.toInt() }
        }
    )

    fun getExternalId(
        portalRequestsCount: BigInt,
        finalNetwork: Network,
        revertableAddress: EthereumAddress
    ): Hex32String {
        val internalIdData = network.portalAddress.byteArray +
                HexString(portalRequestsCount).fillToHex32().byteArray +
                HexString(network.chainId).fillToHex32().byteArray

        val internalId = internalIdData
            .digestKeccak(KeccakParameter.KECCAK_256)
            .let(::Hex32String)

        return getExternalId(internalId, finalNetwork, revertableAddress)
    }

    fun getExternalId(
        internalId: Hex32String,
        finalNetwork: Network,
        revertableAddress: EthereumAddress
    ): Hex32String {
        val externalIdData = internalId.byteArray +
                finalNetwork.synthesizeAddress.byteArray +
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

    fun synthesizeCompletedEventFilter(
        requestId: Hex32String,
        finalNetwork: Network,
        revertableAddress: EthereumAddress
    ): List<Hex32String> = listOf(
        wrapped.hashEventSignature(event = "SynthesizeCompleted"),
        getExternalId(requestId, finalNetwork, revertableAddress)
    )

    fun burnRequestEventFilter(
        address: WalletAddress,
        otherChainId: BigInt? = null
    ): List<Hex32String?> = listOf(
        wrapped.hashEventSignature(event = "BurnRequest"),
        address.fillToHex32(),
        otherChainId?.let(::Hex32String)
    )

    fun getBurnRequestsRequest(
        address: WalletAddress,
        fromChainId: BigInt? = null,
        fromBlock: BlockState? = null,
        toBlock: BlockState? = null
    ) = wrapped.getLogsRequest(fromBlock, toBlock, topics = burnRequestEventFilter(address, fromChainId))

    suspend fun getBurnRequests(
        address: WalletAddress,
        otherChainId: BigInt? = null,
        fromBlock: BlockState? = null,
        toBlock: BlockState? = null
    ): List<LogEvent> = executor
        .executeBatch(getBurnRequestsRequest(address, otherChainId, fromBlock, toBlock))
        .first()
}
