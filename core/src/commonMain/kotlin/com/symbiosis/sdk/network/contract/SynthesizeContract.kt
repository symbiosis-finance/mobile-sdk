@file:OptIn(ExperimentalStdlibApi::class)

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


class SynthesizeContract internal constructor(
    private val executor: Web3Executor,
    private val network: Network,
    private val wrapped: SmartContract,
    private val nonceController: NonceController,
    private val tokenContractFactory: (ContractAddress) -> TokenContract,
    private val defaultGasProvider: GasProvider
) {
    val address: ContractAddress = wrapped.contractAddress
    suspend fun burnSynthTokens(
        credentials: Credentials,
        stableBridgingFee: BigInt,
        amount: BigInt,
        synthCurrencyAddress: ContractAddress,
        targetNetwork: Network,
        gasProvider: GasProvider? = null
    ): TransactionHash {
        tokenContractFactory(synthCurrencyAddress)
            .approveMaxIfNeed(credentials, wrapped.contractAddress, amount, gasProvider)

        return wrapped.write(
            chainId = network.chainId,
            nonceController = nonceController,
            credentials = credentials,
            method = "burnSyntheticToken",
            params = listOf(
                stableBridgingFee,
                synthCurrencyAddress.bigInt,
                amount,
                credentials.address.bigInt,
                targetNetwork.portalAddress.bigInt,
                targetNetwork.bridgeAddress.bigInt,
                credentials.address.bigInt,
                targetNetwork.chainId,
            ),
            gasProvider = gasProvider ?: defaultGasProvider,
            value = 10_000_000_000_000_000.bi
        )
    }

    fun getMetaBurnSynthTokenCalldata(
        stableBridgingFee: BigInt,
        amount: BigInt,
        fromAddress: EthereumAddress,
        finalDexRouter: ContractAddress,
        sToken: ContractAddress,
        finalCallData: HexString?,
        finalOffset: BigInt?,
        chain2Address: EthereumAddress,
        receiveSide: ContractAddress,
        oppositeBridge: ContractAddress,
        chainId: BigInt
    ): HexString = wrapped.encodeMethod(
        method = "metaBurnSyntheticToken",
        params = listOf(
            listOf(
                stableBridgingFee,
                amount,
                fromAddress.bigInt,
                finalDexRouter.bigInt,
                sToken.bigInt,
                finalCallData?.byteArray ?: byteArrayOf(),
                finalOffset ?: 0.bi,
                chain2Address.bigInt,
                receiveSide.bigInt,
                oppositeBridge.bigInt,
                chain2Address.bigInt,
                chainId
            )
        )
    )

    fun getMetaMintSyntheticTokenCalldata(
        to: EthereumAddress,
        portalRequestsCount: BigInt,
        finalNetwork: Network,
        finalSwapCallData: HexString?,
        finalSwapOffset: BigInt,
        swapTokens: List<ContractAddress>,
        stableSwapCallData: HexString,
        stablePoolAddress: ContractAddress,
        firstSwapAmountOut: BigInt,
        firstStableToken: ContractAddress,
    ): HexString = wrapped.encodeMethod(
        method = "metaMintSyntheticToken",
        params = listOf(
            listOf(
                1.bi, // bridging fee
                firstSwapAmountOut, // amount
                getExternalId(portalRequestsCount, finalNetwork, to).byteArray, // externalId
                firstStableToken.bigInt, // rtoken
                network.chainId, // chainId
                to.bigInt,
                swapTokens.map(ContractAddress::bigInt), // swap tokens
                stablePoolAddress.bigInt, // second dex router
                stableSwapCallData.byteArray, // second swap call data
                finalNetwork.routerAddress.bigInt,
                finalSwapCallData?.byteArray ?: byteArrayOf(),
                finalSwapOffset
            )
        )
    )

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
            receiveSide.bigInt,
            oppositeBridge.bigInt,
            chainIdFrom
        )
    )

    suspend fun revertSynthesizeRequest(
        credentials: Credentials,
        stableBridgingFee: BigInt,
        internalId: Hex32String,
        receiveSide: ContractAddress,
        oppositeBridge: ContractAddress,
        chainIdFrom: BigInt,
        gasProvider: GasProvider = defaultGasProvider
    ) = revertSynthesizeRequestRequest(stableBridgingFee, internalId, receiveSide, oppositeBridge, chainIdFrom)
        .write(credentials, network.chainId, gasProvider, nonceController)

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
                recipient = EthereumAddress.createInstance(recipient as BigInt),
                chain2address = WalletAddress.createInstance(chain2address as BigInt),
                amount = amount as BigInt,
                tokenAddress = ContractAddress.createInstance(token as BigInt),
                sTokenAddress = ContractAddress.createInstance(stoken as BigInt),
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
            StuckRequest.State.values().first { it.value == state.toInt() }
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
