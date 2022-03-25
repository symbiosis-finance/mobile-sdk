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

class BurnCrossChainSwapDirection(
    private val firstNetworkClient: NetworkClient,
    private val finalNetworkClient: NetworkClient
) : CrossChainSwapDirection {
    override fun provideStableSwapCallData(
        pool: NerveStablePool,
        amountIn: BigInt,
        deadline: BigInt
    ): HexString = firstNetworkClient
        .getNerveContract(pool)
        .getSwapCallData(
            tokenIndexFrom = 0.bi,
            tokenIndexTo = 1.bi,
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
        .synthesize
        .getMetaBurnSynthTokenCalldata(
            stableBridgingFee = stableBridgingFee,
            amount = stablePoolAmountOut,
            fromAddress = fromAddress,
            finalDexRouter = finalDexRouter,
            sToken = stablePoolRoute.last(),
            swapCallData = finalSwapCallData,
            chain2Address = fromAddress,
            receiveSide = finalNetworkClient.network.portalAddress,
            oppositeBridge = oppositeBridge,
            chainId = finalNetworkChainId
        )

    override suspend fun getStablePoolRoute(pool: NerveStablePool): List<ContractAddress> = pool
        .getPoolRoute(firstNetworkClient.synthFabric)
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
            secondSwapCallData = stableSwapCallData,
            approvedTokens = listOf(
                (firstToken as? MetaRouterV2Contract.FirstToken.Erc20)?.address
                    ?: AddressZero,
                stablePoolRoute.first(),
                stablePoolRoute.last()
            ),
            firstDexRouter = firstDexRouter,
            secondDexRouter = stableDexRouter,
            amount = amountIn,
            nativeIn = firstToken is MetaRouterV2Contract.FirstToken.Native,
            relayRecipient = firstNetworkClient.network.synthesizeAddress,
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
        .portal
        .getMetaUnsynthesizeCalldata(
            token = stablePoolTokens.last().tokenAddress,
            amount = finalSwapAmountIn,
            to = to,
            synthesisRequestsCount = firstNetworkClient.synthesize.requestsCount(),
            finalNetwork = finalNetworkClient.network,
            finalSwapCalldata = finalSwapCallData,
            finalRouterAddress = finalRouterAddress
        )
}
