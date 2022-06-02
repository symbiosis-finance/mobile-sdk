package com.symbiosis.sdk.swap.crosschain.executor

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.ClientsManager
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.swap.crosschain.CrossChain
import com.symbiosis.sdk.swap.crosschain.SingleNetworkSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.StableSwapTradeAdapter
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.TransactionHash
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
    ): TransactionHash {
        return ClientsManager
            .getNetworkClient(crossChain.fromNetwork)
            .metaRouter
            .metaRoute(
                credentials = credentials,
                firstSwapCallData = inputTrade.callData,
                secondSwapCallData = directionAdapter.secondSwapCallData(deadline),
                approvedTokens = directionAdapter.approvedTokens,
                firstDexRouter = inputTrade.routerAddress,
                secondDexRouter = stableTrade.routerAddress,
                amount = inputTrade.amountIn,
                nativeIn = nativeIn,
                relayRecipient = directionAdapter.relayRecipient,
                otherSideCallData = directionAdapter.otherSideCallData(deadline),
                gasProvider = gasProvider
            )
    }

    interface DirectionAdapter {
        suspend fun secondSwapCallData(deadline: BigInt?): HexString?
        val approvedTokens: List<ContractAddress>
        val relayRecipient: ContractAddress
        suspend fun otherSideCallData(deadline: BigInt?): HexString
    }
}
