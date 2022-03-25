package com.symbiosis.sdk.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.ClientsManager
import com.symbiosis.sdk.configuration.BridgingFeeProvider
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.currency.AddressZero
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.NativeToken
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.currency.amount
import com.symbiosis.sdk.currency.convertIntegerToReal
import com.symbiosis.sdk.currency.convertRealToInteger
import com.symbiosis.sdk.currency.thisOrWrapped
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.contract.RouterContract
import com.symbiosis.sdk.network.contract.metaRouter.MetaRouterV2Contract
import com.symbiosis.sdk.network.contract.provideDeadline
import com.symbiosis.sdk.swap.CalculatedSwapTrade
import com.symbiosis.sdk.swap.amountInMax
import com.symbiosis.sdk.swap.amountOutMin
import com.symbiosis.sdk.swap.meta.CalculatedMetaSwapTrade
import com.symbiosis.sdk.swap.meta.MetaSwapRoute
import com.symbiosis.sdk.swap.meta.NerveStablePool
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.hex.HexString

@RequiresOptIn(
    message = "Please, consider to use ClientsManager.getCrossChainClient instead of the raw constructor, because " +
            "it's signature may be changed in the future.",
    level = RequiresOptIn.Level.WARNING
)
annotation class RawUsageOfCrossChainConstructor

class CrossChainClient @RawUsageOfCrossChainConstructor constructor(val crossChain: CrossChain) {
    private val firstNetworkClient = ClientsManager.getNetworkClient(crossChain.fromNetwork)
    private val finalNetworkClient = ClientsManager.getNetworkClient(crossChain.toNetwork)

    private val burnDirection: CrossChainSwapDirection =
        BurnCrossChainSwapDirection(firstNetworkClient, finalNetworkClient)
    private val synthDirection: CrossChainSwapDirection =
        SynthCrossChainSwapDirection(firstNetworkClient, finalNetworkClient)


    private val firstNetwork = firstNetworkClient.network
    private val finalNetwork = finalNetworkClient.network

    private suspend fun deadline() = provideDeadline(crossChain.ttlProvider.getSwapTTL(firstNetwork.chainId))

    init {
        require(firstNetwork.chainId != finalNetwork.chainId) {
            "CrossChain swap should be from different networks"
        }
    }

    @Throws(Throwable::class)
    suspend fun getAllowedRawRangeForInput(
        fromToken: Token,
        slippage: BigNum = 0.07.bn,
    ): Pair</* min */ BigInt, /* max */ BigInt> {
        require(fromToken.network.chainId == firstNetwork.chainId) {
            "fromToken is from invalid network (${fromToken.network.networkName}, but required ${firstNetwork.networkName})"
        }

        val (minTrade, cache) = firstNetworkClient.swap.findBestTradeExactOut(
            networkTokenPair = NetworkTokenPair(
                first = fromToken,
                second = crossChain.fromToken
            ),
            amountOut = crossChain.fromToken.convertRealToInteger(crossChain.minStableTokensAmountPerTrade)
        )
        minTrade as? CalculatedSwapTrade.ExactOut.Success
            ?: error("Invalid crosschain configuration or unexpected low liquidity")

        val maxTrade = firstNetworkClient.swap.findBestTradeExactOutCached(
            routes = cache,
            amountOut = crossChain.fromToken.convertRealToInteger(crossChain.maxStableTokensAmountPerTrade)
        )
        maxTrade as? CalculatedSwapTrade.ExactOut.Success
            ?: error("Invalid crosschain configuration or unexpected low liquidity")

        return minTrade.amountInMax(slippage) to maxTrade.amountInMax(slippage)
    }

    @Throws(Throwable::class)
    suspend fun getAllowedRangeForInput(
        fromToken: DecimalsToken,
        slippage: BigNum = 0.07.bn,
    ): Pair</* min */ TokenAmount, /* max */ TokenAmount> =
        getAllowedRawRangeForInput(fromToken as Token, slippage)
            .let { (min, max) -> fromToken.amount(min) to fromToken.amount(max) }

    /**
     * find Best trade using meta router
     * @param fromToken token we want to swap from
     * @param targetToken token we want to receive
     * @param amountIn desired amount of fromToken
     * @param slippage default valued tolerance
     **/
    @Throws(Throwable::class)
    suspend fun findBestTradeExactIn(
        to: WalletAddress,
        fromToken: Token,
        targetToken: Token,
        amountIn: BigInt,
        slippage: BigNum = 0.07.bn,
        fromNetworkDexEndpoints: List<DexEndpoint> = crossChain.fromNetwork.dexEndpoints,
        toNetworkDexEndpoints: List<DexEndpoint> = crossChain.toNetwork.dexEndpoints,
        stablePool: NerveStablePool = crossChain.stablePool,
        bridgingFeeProvider: BridgingFeeProvider = crossChain.bridgingFeeProvider,
        // used for recursive call
        bridgingFee: BigInt? = null,
        // how many times this function should be
        // called recursively
        coerceTimes: Int = 1
    ): CalculatedMetaSwapTrade? {
        require(slippage < 1.bn && slippage >= 0.bn) { "Tolerance should be in [0;1) range but was $slippage" }

        require(fromToken.network.chainId == firstNetwork.chainId) {
            "fromToken is from invalid network (${fromToken.network.networkName}, but required ${firstNetwork.networkName})"
        }
        require(targetToken.network.chainId == finalNetwork.chainId) {
            "targetToken is from invalid network (${targetToken.network.networkName}, but required ${finalNetwork.networkName})"
        }

        val nerve = firstNetworkClient.getNerveContract(stablePool)
        val targetNetworkNerve = finalNetworkClient.getNerveContract(stablePool)

        // For example if we requested BNB -> ETH, but got ETH -> BNB because the last one stable pool is cheaper
        val stablePoolLocation = when (stablePool.fromNetwork.chainId == firstNetwork.chainId) {
            true -> MetaSwapRoute.StablePoolLocation.FirstNetwork
            false -> MetaSwapRoute.StablePoolLocation.LastNetwork
        }

        val direction = when (stablePoolLocation) {
            MetaSwapRoute.StablePoolLocation.FirstNetwork -> burnDirection
            MetaSwapRoute.StablePoolLocation.LastNetwork -> synthDirection
        }

        // If the network of the pool is different from the first token network, we need to go to targetToken,
        // then synthesize and make trade on pool on different network,
        // otherwise we need to go to fromToken, then make trade and then burn synths to second network
        val lastTokenToStable = when (stablePoolLocation) {
            MetaSwapRoute.StablePoolLocation.FirstNetwork -> stablePool.fromToken
            MetaSwapRoute.StablePoolLocation.LastNetwork -> stablePool.targetToken
        }.also(::println)
        // Path to stable pool from first token
        val pathToStable = when (fromToken.thisOrWrapped.tokenAddress) {
            lastTokenToStable.tokenAddress -> null // we are already on stable pool token, so there is no trade
            else -> firstNetworkClient.swap.findBestTradeExactIn(
                networkTokenPair = NetworkTokenPair(
                    first = fromToken,
                    second = lastTokenToStable
                ),
                amountIn = amountIn,
                dexEndpoints = fromNetworkDexEndpoints
            ).first ?: return null
        }

        val (stablePoolExecutor, stablePoolNerve) = when (stablePoolLocation) {
            MetaSwapRoute.StablePoolLocation.FirstNetwork -> firstNetworkClient to nerve
            MetaSwapRoute.StablePoolLocation.LastNetwork -> finalNetworkClient to targetNetworkNerve
        }
        val stablePoolAmountIn = when (stablePoolLocation) {
            MetaSwapRoute.StablePoolLocation.FirstNetwork -> pathToStable?.amountOutMin(slippage) ?: amountIn
            MetaSwapRoute.StablePoolLocation.LastNetwork ->
                ((pathToStable?.amountOutMin(slippage) ?: amountIn) - (bridgingFee ?: 0.bi))
                    .let { number -> if (number > 0.bi) number else 0.bi }
        }
        // Tokens in the pool is being determined by index, so in busd -> usdc pool,
        // bsc will have index 0, while usdc will have index 1,
        // so here we determine indexed corresponding to trade type
        //
        // To calculate amountOutMin of our trade, we should perform 2 calculations: with normal amountOut and with min
        val stablePathAmountOutMin =
            stablePoolNerve.calculateSwap(
                tokenIndexFrom = if (stablePoolLocation == MetaSwapRoute.StablePoolLocation.FirstNetwork) 0 else 1,
                tokenIndexTo = if (stablePoolLocation == MetaSwapRoute.StablePoolLocation.FirstNetwork) 1 else 0,
                amount = stablePoolAmountIn
            )
        //
        val firstTokenFromStable = when (stablePoolLocation) {
            MetaSwapRoute.StablePoolLocation.FirstNetwork -> stablePool.targetToken
            MetaSwapRoute.StablePoolLocation.LastNetwork -> stablePool.fromToken
        }

        val pathFromStableAmountIn = when (stablePoolLocation) {
            MetaSwapRoute.StablePoolLocation.FirstNetwork ->
                (stablePathAmountOutMin - (bridgingFee ?: 0.bi))
                    .let { number -> if (number > 0.bi) number else 0.bi }
            MetaSwapRoute.StablePoolLocation.LastNetwork -> stablePathAmountOutMin
        }

        // the last part of path: from the stable pool to the target currency
        val pathFromStable: CalculatedSwapTrade.ExactIn? =
            when (targetToken.thisOrWrapped.tokenAddress) {
                firstTokenFromStable.tokenAddress -> null // we are already on stable pool
                else -> finalNetworkClient.swap.findBestTradeExactIn(
                    networkTokenPair = NetworkTokenPair(
                        first = firstTokenFromStable,
                        second = targetToken
                    ),
                    amountIn = pathFromStableAmountIn,
                    dexEndpoints = toNetworkDexEndpoints
                ).first
            }

        val stableSwapCallData = direction.provideStableSwapCallData(
            pool = stablePool,
            amountIn = stablePoolAmountIn,
            deadline = deadline()
        )

        val finalSwapCallData: HexString? = pathFromStable?.let { path ->
            provideUniLikeSwapCallData(
                amountIn = pathFromStableAmountIn,
                amountOutMin = path.amountOutMin(slippage),
                path = path.route.value,
                routerContract = finalNetworkClient.router,
                deadline = deadline(),
                recipient = to,
            )
        }

        if (bridgingFee == null || coerceTimes > 0)
            return findBestTradeExactIn(
                to, fromToken, targetToken, amountIn, slippage, fromNetworkDexEndpoints,
                toNetworkDexEndpoints, stablePool, bridgingFeeProvider,
                bridgingFee = bridgingFee ?: bridgingFeeProvider
                    .getBridgingFee(
                        chainFromId = firstNetwork.chainId,
                        chainToId = finalNetwork.chainId,
                        receiveSide = when (stablePoolLocation) {
                            MetaSwapRoute.StablePoolLocation.FirstNetwork -> finalNetwork.portalAddress
                            MetaSwapRoute.StablePoolLocation.LastNetwork -> finalNetwork.synthesizeAddress
                        },
                        callData = direction.buildBridgeCallData(
                            firstSwapAmountOut = pathFromStable?.amountOut ?: amountIn,
                            stableSwapRoute = when (stablePoolLocation) {
                                MetaSwapRoute.StablePoolLocation.FirstNetwork ->
                                    stablePool.getPoolRoute(firstNetworkClient.synthFabric)
                                        .addressList
                                MetaSwapRoute.StablePoolLocation.LastNetwork ->
                                    stablePool.getPoolRoute(finalNetworkClient.synthFabric)
                                        .addressList
                            },
                            stablePoolTokens = stablePool.tokens,
                            stableSwapCallData = stableSwapCallData,
                            stablePoolAddress = stablePool.address,
                            finalSwapAmountIn = pathFromStableAmountIn,
                            to = to,
                            finalSwapCallData = finalSwapCallData,
                            finalRouterAddress = finalNetwork.routerAddress
                        )
                    ),
                coerceTimes = coerceTimes - 1
            )

        val stableTokensAmount = when (stablePool.targetToken.tokenAddress) {
            lastTokenToStable.tokenAddress -> lastTokenToStable.convertIntegerToReal(stablePoolAmountIn)
            firstTokenFromStable.tokenAddress -> firstTokenFromStable.convertIntegerToReal(stablePathAmountOutMin)
            else -> error("Invalid internal state")
        }

        return when {
            stableTokensAmount < crossChain.minStableTokensAmountPerTrade ->
                CalculatedMetaSwapTrade.StableTokensLessThanMin(
                    tradeStableTokens = stableTokensAmount,
                    minStableTokens = crossChain.minStableTokensAmountPerTrade
                )
            stableTokensAmount > crossChain.maxStableTokensAmountPerTrade ->
                CalculatedMetaSwapTrade.StableTokensGreaterThanMax(
                    tradeStableTokens = stableTokensAmount,
                    maxStableTokens = crossChain.maxStableTokensAmountPerTrade
                )
            else -> CalculatedMetaSwapTrade.ExactIn(
                amountIn = amountIn,
                route = MetaSwapRoute(
                    pathToStable = pathToStable,
                    firstNetworkStableToken = lastTokenToStable,
                    lastNetworkStableToken = firstTokenFromStable,
                    pathFromStable = pathFromStable,
                    stablePoolLocation = stablePoolLocation,
                    nerveAddress = stablePool.address
                ),
                stablePoolAmountIn = stablePoolAmountIn,
                stablePoolAmountOutMin = stablePathAmountOutMin,
                pathFromStableAmountIn = pathFromStableAmountIn,
                amountOut = pathFromStable?.amountOut ?: pathFromStableAmountIn,
                amountOutMin = pathFromStable?.amountOutMin(slippage) ?: pathFromStableAmountIn,
                slippage = slippage,
                bridgingFee = bridgingFee
            )
        }
    }

    @Throws(Throwable::class)
    suspend fun execute(
        credentials: Credentials,
        trade: CalculatedMetaSwapTrade.ExactIn,
        stablePool: NerveStablePool = crossChain.stablePool,
        deadline: BigInt? = null,
        gasProvider: GasProvider = firstNetwork.gasProvider
    ): CrossChainSwapTransaction {
        require(
            trade.route.fromNetwork.chainId == firstNetwork.chainId &&
                    trade.route.toNetwork.chainId == finalNetwork.chainId
        ) {
            error(
                "The trade was calculated using different cross chain. Trade networks: ${trade.route.fromNetwork}" +
                        " -> ${trade.route.toNetwork}. Executor networks: $firstNetwork -> $finalNetwork"
            )
        }
        val deadlineOrDefault = deadline ?: deadline()

        val direction = when (trade.route.stablePoolLocation) {
            MetaSwapRoute.StablePoolLocation.FirstNetwork -> burnDirection
            MetaSwapRoute.StablePoolLocation.LastNetwork -> synthDirection
        }

        val firstSwapCallData: HexString? = trade.route.pathToStable?.let { path ->
            provideUniLikeSwapCallData(
                amountIn = trade.value,
                amountOutMin = path.amountOutMin(trade.slippage),
                path = path.route.value,
                recipient = firstNetwork.metaRouterAddress,
                routerContract = firstNetworkClient.router,
                deadline = deadlineOrDefault
            )
        }

        val stablePoolRoute = direction.getStablePoolRoute(stablePool)

        val stableSwapCallData = direction.provideStableSwapCallData(
            pool = stablePool,
            amountIn = trade.stablePoolAmountIn,
            deadline = deadlineOrDefault
        )

        val finalSwapCallData: HexString? = trade.route.pathFromStable?.let { path ->
            provideUniLikeSwapCallData(
                amountIn = trade.pathFromStableAmountIn,
                amountOutMin = path.amountOutMin(trade.slippage),
                path = path.route.value,
                recipient = credentials.address,
                routerContract = finalNetworkClient.router,
                deadline = deadlineOrDefault
            )
        }

        val otherSideCallData: HexString = direction.getOtherSideCallData(
            stableBridgingFee = trade.bridgingFee,
            stablePoolAmountIn = trade.stablePoolAmountIn,
            stablePoolAmountOut = trade.stablePoolAmountOutMin,
            fromAddress = credentials.address,
            finalDexRouter = finalNetwork.routerAddress,
            stableSwapCallData = stableSwapCallData,
            finalSwapCallData = finalSwapCallData,
            oppositeBridge = finalNetwork.bridgeAddress,
            finalNetworkChainId = finalNetwork.chainId,
            stablePool = stablePool,
            stablePoolRoute = stablePoolRoute
        )

        val firstToken = trade.route.pathToStable?.route?.value?.first()
            ?: trade.route.firstNetworkStableToken

        val txHash: TransactionHash = direction.metaRoute(
            credentials = credentials,
            firstToken = when (firstToken) {
                is NativeToken -> MetaRouterV2Contract.FirstToken.Native
                is Erc20Token -> MetaRouterV2Contract.FirstToken.Erc20(firstToken.tokenAddress)
            },
            stablePool = stablePool,
            stablePoolRoute = stablePoolRoute,
            firstNetworkChainId = firstNetwork.chainId,
            firstSwapCallData = firstSwapCallData,
            stableSwapCallData = stableSwapCallData,
            firstDexRouter = firstNetwork.routerAddress,
            stableDexRouter = stablePool.address,
            amountIn = trade.value,
            otherSideCallData = otherSideCallData,
            gasProvider = gasProvider
        )

        return CrossChainSwapTransaction(
            hash = txHash,
            fromExecutor = firstNetworkClient,
            targetExecutor = finalNetworkClient,
            targetPortal = finalNetworkClient.portal,
            targetSynthesize = finalNetworkClient.synthesize,
            swap = trade,
            revertableAddress = credentials.address
        )
    }

    private suspend fun provideUniLikeSwapCallData(
        amountIn: BigInt,
        amountOutMin: BigInt,
        path: List<Token>,
        routerContract: RouterContract,
        deadline: BigInt,
        recipient: EthereumAddress,
    ): HexString {
        return when {
            // if it has native tokens, then choose method with native token on start or at the end
            path.first() is NativeToken -> routerContract.getSwapExactNativeForTokensCallData(
                amountOutMin = amountOutMin,
                path = path.map(Token::thisOrWrapped).map(Erc20Token::tokenAddress),
                recipient = recipient,
                deadline = deadline
            )
            path.last() is NativeToken -> routerContract.getSwapExactTokensForNativeCallData(
                amountIn = amountIn,
                amountOutMin = amountOutMin,
                path = path.map(Token::thisOrWrapped).map(Erc20Token::tokenAddress),
                deadline = deadline,
                recipient = recipient
            )
            else -> routerContract.getSwapExactTokensForTokensCallData(
                amountIn = amountIn,
                amountOutMin = amountOutMin,
                path = path.map(Token::thisOrWrapped).map(Erc20Token::tokenAddress),
                deadline = deadline,
                recipient = recipient
            )
        }
    }

    private val WalletAddress.Companion.Zero
        get() =
            WalletAddress(AddressZero.prefixed)
}
