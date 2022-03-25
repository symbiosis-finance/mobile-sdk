package com.symbiosis.sdk.crosschain

import com.symbiosis.sdk.network.contract.PortalContract
import com.symbiosis.sdk.network.contract.SynthesizeContract
import com.symbiosis.sdk.network.contract.requireBurnRequestEvent
import com.symbiosis.sdk.network.contract.requireSynthesizeRequestEvent
import com.symbiosis.sdk.swap.meta.CalculatedMetaSwapTrade
import com.symbiosis.sdk.swap.meta.MetaSwapRoute
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.entity.TransactionReceipt
import dev.icerock.moko.web3.hex.Hex32String
import dev.icerock.moko.web3.requests.polling.newLogsShortPolling
import dev.icerock.moko.web3.requests.waitForTransactionReceipt
import kotlinx.coroutines.flow.first

class CrossChainSwapTransaction(
    val hash: TransactionHash,
    private val fromExecutor: Web3Executor,
    private val targetExecutor: Web3Executor,
    private val targetPortal: PortalContract,
    private val targetSynthesize: SynthesizeContract,
    private val swap: CalculatedMetaSwapTrade.Success,
    private val revertableAddress: EthereumAddress
) {
    /**
     * @return null if is not success
     */
    suspend fun waitForCompletion(): LogEvent? =
        waitForCompletion(waitForReceipt())

    // fixme: add from network executor
    suspend fun waitForReceipt(): TransactionReceipt = fromExecutor.waitForTransactionReceipt(hash)

    suspend fun waitForCompletion(receipt: TransactionReceipt): LogEvent? {
        if (receipt.status != TransactionReceipt.Status.SUCCESS)
            return null

        val requestId: Hex32String = when (swap.route.stablePoolLocation) {
            MetaSwapRoute.StablePoolLocation.FirstNetwork -> receipt.logs.requireBurnRequestEvent()
            MetaSwapRoute.StablePoolLocation.LastNetwork -> receipt.logs.requireSynthesizeRequestEvent()
        }.deserializeData { data -> data[0] }

        return waitForCompletion(requestId)
    }

    suspend fun waitForCompletion(requestId: Hex32String): LogEvent = targetExecutor.newLogsShortPolling(
        topics = when (swap.route.stablePoolLocation) {
            MetaSwapRoute.StablePoolLocation.FirstNetwork ->
                targetPortal.burnCompletedEventFilter(requestId, swap.route.toNetwork, revertableAddress)
            MetaSwapRoute.StablePoolLocation.LastNetwork ->
                targetSynthesize.synthesizeCompletedEventFilter(requestId, swap.route.toNetwork, revertableAddress)
        },
        address = when (swap.route.stablePoolLocation) {
            MetaSwapRoute.StablePoolLocation.FirstNetwork -> targetPortal.address
            MetaSwapRoute.StablePoolLocation.LastNetwork -> targetSynthesize.address
        }
    ).first()
}
