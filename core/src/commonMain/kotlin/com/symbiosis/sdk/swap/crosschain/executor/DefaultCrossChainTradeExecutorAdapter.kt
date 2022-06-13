package com.symbiosis.sdk.swap.crosschain.executor

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.network.networkClient
import com.symbiosis.sdk.swap.crosschain.CrossChain
import com.symbiosis.sdk.swap.crosschain.SingleNetworkSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.StableSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.transaction.CrossChainSwapTransaction
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.Web3RpcException
import dev.icerock.moko.web3.hex.HexString

class DefaultCrossChainTradeExecutorAdapter(
    private val crossChain: CrossChain,
    private val inputTrade: SingleNetworkSwapTradeAdapter,
    private val stableTrade: StableSwapTradeAdapter,
    private val directionAdapter: DirectionAdapter,
    private val nativeIn: Boolean
) : CrossChainTradeExecutorAdapter {
    override suspend fun execute(
        credentials: Credentials,
        deadline: BigInt?,
        gasProvider: GasProvider?
    ): CrossChainTradeExecutorAdapter.ExecuteResult =
        try {
            val txHash = crossChain
                .fromNetwork
                .networkClient
                .metaRouter
                .metaRoute(
                    credentials = credentials,
                    firstSwapCallData = inputTrade.callData,
                    secondSwapCallData = directionAdapter.secondSwapCallData(deadline),
                    approvedTokens = directionAdapter.approvedTokens,
                    firstDexRouter = inputTrade.routerAddress,
                    secondDexRouter = stableTrade.routerAddress,
                    amount = inputTrade.amountIn.raw,
                    nativeIn = nativeIn,
                    relayRecipient = directionAdapter.relayRecipient,
                    otherSideCallData = directionAdapter.otherSideCallData(deadline),
                    gasProvider = gasProvider
                )

            val transaction = CrossChainSwapTransaction(txHash, credentials.address, crossChain)
            CrossChainTradeExecutorAdapter.ExecuteResult.Sent(transaction)
        } catch (_: Web3RpcException) {
            CrossChainTradeExecutorAdapter.ExecuteResult.ExecutionRevertedWithoutSending
        }

    interface DirectionAdapter {
        suspend fun secondSwapCallData(deadline: BigInt?): HexString?
        val approvedTokens: List<ContractAddress>
        val relayRecipient: ContractAddress
        suspend fun otherSideCallData(deadline: BigInt?): HexString
    }
}
