package com.symbiosis.sdk.stuck

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.network.contract.OutboundRequest
import com.symbiosis.sdk.network.contract.PortalContract
import com.symbiosis.sdk.network.contract.SynthesizeContract
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.hex.Hex32String

class StuckRequest(
    val internalId: Hex32String,
    val externalId: Hex32String,
    val request: OutboundRequest,
    val state: State, // Default | Reverted
    val fromClient: NetworkClient,
    val targetClient: NetworkClient,
) {
    init {
        require(state != State.Completed) { "This transaction is not stuck" }
    }

    suspend fun revert(
        credentials: Credentials,
        gasProvider: GasProvider = targetClient.network.gasProvider
    ): RevertTransaction {
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
        }

//        val bridgingFee = bridgingFeeProvider.getBridgingFee(
//            chainFromId = targetClient.network.chainId,
//            chainToId = fromClient.network.chainId,
//            receiveSide = receiveSide,
//            callData = relayersCallData
//        )

        val bridgingFee: BigInt = TODO()

        val transactionHash = when (request) {
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

        return RevertTransaction(request = this, transactionHash)
    }

    enum class State(val value: Int) {
        Default(value = 0),
        Completed(value = 1), // Synthesized | Unsynthesized
        Reverted(value = 2)
    }
}
