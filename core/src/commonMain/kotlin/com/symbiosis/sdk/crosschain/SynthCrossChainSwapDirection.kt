package com.symbiosis.sdk.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.currency.AddressZero
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.currency.thisOrWrapped
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.network.contract.metaRouter.MetaRouterV2Contract
import com.symbiosis.sdk.swap.meta.NerveStablePool
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.hex.HexString

class SynthCrossChainSwapDirection(
    private val firstNetworkClient: NetworkClient,
    private val finalNetworkClient: NetworkClient
) : CrossChainSwapDirection {
    override fun provideStableSwapCallData(
        pool: NerveStablePool,
        amountIn: BigInt,
        deadline: BigInt
    ): HexString = finalNetworkClient
        .getNerveContract(pool)
        .getSwapCallData(
            tokenIndexFrom = 1.bi,
            tokenIndexTo = 0.bi,
            dx = amountIn,
            minDy = 0.bi,
            deadline = deadline
        )

    override suspend fun getOtherSideCallData(
        stableBridgingFee: BigInt,
        stablePoolAmountIn: BigInt,
        stablePoolAmountOut: BigInt,
        fromAddress: WalletAddress,
        finalDexRouter: ContractAddress,
        stableSwapCallData: HexString,
        finalSwapCallData: HexString?,
        oppositeBridge: ContractAddress,
        finalNetworkChainId: BigInt,
        stablePool: NerveStablePool,
        stablePoolRoute: List<ContractAddress>
    ): HexString = firstNetworkClient
        .portal
        .getMetaSynthesizeCalldata(
            stableBridgingFee = stableBridgingFee,
            amount = stablePoolAmountIn,
            rtoken = stablePool.targetToken.tokenAddress,
            chain2address = fromAddress,
            receiveSide = finalNetworkClient.network.synthesizeAddress,
            oppositeBridge = oppositeBridge,
            fromAddress = fromAddress,
            chainId = finalNetworkChainId,
            swapTokens = stablePoolRoute.reversed() +
                    when (finalSwapCallData) {
                        null -> listOf()
                        else -> listOf(AddressZero)
                    },
            secondDexRouter = stablePool.address,
            secondSwapCalldata = stableSwapCallData,
            finalDexRouter = finalDexRouter,
            finalSwapCalldata = finalSwapCallData
        )

    override suspend fun getStablePoolRoute(pool: NerveStablePool): List<ContractAddress> = pool
        .getPoolRoute(finalNetworkClient.synthFabric)
        .addressList

    override suspend fun metaRoute(
        credentials: Credentials,
        firstToken: MetaRouterV2Contract.FirstToken,
        stablePool: NerveStablePool,
        stablePoolRoute: List<ContractAddress>,
        firstNetworkChainId: BigInt,
        firstSwapCallData: HexString?,
        stableSwapCallData: HexString,
        firstDexRouter: ContractAddress,
        stableDexRouter: ContractAddress,
        amountIn: BigInt,
        otherSideCallData: HexString,
        gasProvider: GasProvider
    ): TransactionHash = firstNetworkClient
        .metaRouterV2
        .metaRouteV2(
            credentials = credentials,
            firstToken = firstToken,
            chainId = firstNetworkChainId,
            firstSwapCallData = firstSwapCallData,
            secondSwapCallData = null,
            approvedTokens = listOf(
                (firstToken as? MetaRouterV2Contract.FirstToken.Erc20)?.address
                    ?: AddressZero,
                stablePool.targetToken.tokenAddress
            ),
            firstDexRouter = firstDexRouter,
            secondDexRouter = stableDexRouter,
            amount = amountIn,
            nativeIn = firstToken is MetaRouterV2Contract.FirstToken.Native,
            relayRecipient = firstNetworkClient.network.portalAddress,
            otherSideCallData = otherSideCallData,
            gasProvider = gasProvider
        )

    override suspend fun buildBridgeCallData(
        firstSwapAmountOut: BigInt,
        stableSwapRoute: List<ContractAddress>,
        stablePoolTokens: List<Erc20Token>,
        stableSwapCallData: HexString,
        stablePoolAddress: ContractAddress,
        finalSwapAmountIn: BigInt,
        to: WalletAddress,
        finalSwapCallData: HexString?,
        finalRouterAddress: ContractAddress
    ): HexString = firstNetworkClient
        .synthesize
        .getMetaMintSyntheticTokenCalldata(
            to = to,
            portalRequestsCount = firstNetworkClient.portal.requestsCount(),
            finalNetwork = finalNetworkClient.network,
            finalSwapCallData = finalSwapCallData,
            stableSwapRoute = stableSwapRoute.reversed(),
            stableSwapCallData = stableSwapCallData,
            stablePoolAddress = stablePoolAddress,
            firstSwapAmountOut = firstSwapAmountOut,
            firstStableToken = stablePoolTokens.last().tokenAddress,
            finalRouterAddress = finalRouterAddress
        )
}
