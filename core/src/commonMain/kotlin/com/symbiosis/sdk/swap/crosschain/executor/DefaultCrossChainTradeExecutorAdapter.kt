package com.symbiosis.sdk.swap.crosschain.executor

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.internal.kbignum.UINT256_MAX
import com.symbiosis.sdk.network.contract.checkTokenAllowance
import com.symbiosis.sdk.network.networkClient
import com.symbiosis.sdk.swap.crosschain.CrossChain
import com.symbiosis.sdk.swap.crosschain.SingleNetworkSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.StableSwapTradeAdapter
import com.symbiosis.sdk.swap.crosschain.transaction.CrossChainSwapTransaction
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.Web3RpcException
import dev.icerock.moko.web3.hex.HexString

class DefaultCrossChainTradeExecutorAdapter(
    private val crossChain: CrossChain,
    private val inputTrade: SingleNetworkSwapTradeAdapter,
    private val stableTrade: StableSwapTradeAdapter,
    private val directionAdapter: DirectionAdapter,
    private val nativeIn: Boolean,
    private val tokens: TokenPair
) : CrossChainTradeExecutorAdapter {

    private val inputClient = tokens.first.network.networkClient

    suspend fun isApproveRequired(walletAddress: WalletAddress): Boolean {
        val inputToken = tokens.first

        if (inputToken !is DecimalsErc20Token)
            return false

        return !inputClient
            .getTokenContract(inputToken.tokenAddress)
            .checkTokenAllowance(walletAddress, inputClient.network.metaRouterGatewayAddress, BigInt.UINT256_MAX)
    }

    suspend fun approveMaxIfRequired(
        credentials: Credentials,
        gasProvider: GasProvider? = null
    ) {
        if (!isApproveRequired(credentials.address))
            return

        inputClient.getTokenContract((tokens.first as DecimalsErc20Token).tokenAddress)
            .approveMax(
                credentials = credentials,
                spender = inputClient.network.metaRouterGatewayAddress,
                gasProvider = gasProvider
            )
    }

    override suspend fun execute(
        credentials: Credentials,
        deadline: BigInt?,
        gasProvider: GasProvider?
    ): CrossChainTradeExecutorAdapter.ExecuteResult =
        try {
            // if you want custom gasProvider here
            // just call this function by yourself, so
            // next call will just check if approval required
            approveMaxIfRequired(credentials)

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
