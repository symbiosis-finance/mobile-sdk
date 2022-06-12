package com.symbiosis.sdk.stuck

import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.network.contract.OutboundRequest
import com.symbiosis.sdk.network.contract.PortalContract
import com.symbiosis.sdk.network.contract.SynthesizeContract
import com.symbiosis.sdk.swap.crosschain.bridging.SymbiosisBridgingApi
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.Web3RpcException
import dev.icerock.moko.web3.hex.Hex32String

class StuckTransaction(
    val internalId: Hex32String,
    val externalId: Hex32String,
    val request: OutboundRequest,
    val state: State, // Default | Reverted
    val fromClient: NetworkClient,
    val targetClient: NetworkClient,
    val advisorUrl: String
) {
    init {
        require(state != State.Completed) { "This transaction is not stuck" }
    }


    sealed interface RevertResult {
        class Sent(val transaction: RevertTransaction) : RevertResult

        /**
         * This may happen if the transaction is not actuall
         */
        object FailedWithoutSending : RevertResult
    }

    suspend fun revert(
        credentials: Credentials,
        gasProvider: GasProvider = targetClient.network.gasProvider
    ): RevertResult {
        val relayersCallData = when (request) {
            is SynthesizeContract.BurnOutboundRequest -> fromClient.synthesize.revertBurnRequest(
                stableBridgingFee = 0.bi,
                externalId = externalId
            )
            is PortalContract.SynthOutboundRequest -> fromClient.portal.revertSynthesizeRequest(
                stableBridgingFee = 0.bi,
                externalId = externalId
            )
            else -> error("impossible state handled")
        }.callData

        val receiveSide = when (request) {
            is SynthesizeContract.BurnOutboundRequest -> fromClient.synthesize.address
            is PortalContract.SynthOutboundRequest -> fromClient.portal.address
            else -> error("Impossible state handled")
        }

        val bridgingFee = SymbiosisBridgingApi.getBridgingFee(
            chainFromId = targetClient.network.chainId,
            chainToId = fromClient.network.chainId,
            receiveSide = receiveSide,
            callData = relayersCallData,
            advisorUrl = advisorUrl
        )

        val transactionHash = try {
             when (request) {
                is SynthesizeContract.BurnOutboundRequest -> targetClient.portal.revertBurnRequest(
                    credentials = credentials,
                    stableBridgingFee = bridgingFee,
                    internalId = internalId,
                    receiveSide = receiveSide,
                    oppositeBridge = fromClient.network.bridgeAddress,
                    chainIdFrom = fromClient.network.chainId,
                    gasProvider = gasProvider
                )
                is PortalContract.SynthOutboundRequest -> targetClient.synthesize.revertSynthesizeRequest(
                    credentials = credentials,
                    stableBridgingFee = bridgingFee,
                    internalId = internalId,
                    receiveSide = receiveSide,
                    oppositeBridge = fromClient.network.bridgeAddress,
                    chainIdFrom = fromClient.network.chainId,
                    gasProvider = gasProvider
                )
                else -> error("impossible state handled")
            }
        } catch (_: Web3RpcException) {
            return RevertResult.FailedWithoutSending
        }

        val transaction = RevertTransaction(request = this, transactionHash)
        return RevertResult.Sent(transaction)
    }

    enum class State(val value: Int) {
        Pending(value = 0),
        Completed(value = 1), // Synthesized | Unsynthesized
        Reverted(value = 2)
    }
}
