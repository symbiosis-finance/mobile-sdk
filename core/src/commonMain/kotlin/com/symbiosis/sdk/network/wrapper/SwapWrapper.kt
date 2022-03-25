package com.symbiosis.sdk.network.wrapper

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.NativeToken
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.currency.thisOrWrapped
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.internal.time.minutesAsMillis
import com.symbiosis.sdk.internal.time.timeMillis
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.network.contract.RouterContract
import com.symbiosis.sdk.swap.CalculatedSwapTrade
import com.symbiosis.sdk.swap.SwapRouteState
import com.symbiosis.sdk.swap.SwapRoutesGenerator
import com.symbiosis.sdk.swap.SwapTrade
import com.symbiosis.sdk.swap.SwapTradeState
import com.symbiosis.sdk.swap.SwapTradesCalculator
import com.symbiosis.sdk.swap.SwapType
import com.symbiosis.sdk.swap.amountInMax
import com.symbiosis.sdk.swap.amountOutMin
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.Web3Executor

/**
 * High-level wrapper around Swap Contracts
 */
class SwapWrapper internal constructor(
    private val network: Network,
    private val executor: Web3Executor,
    private val reservesRequestsFactory: SwapTradesCalculator.ReservesRequestsFactory,
    private val router: RouterContract,
    private val defaultDexEndpoints: List<DexEndpoint>
){
    @Throws(Throwable::class)
    suspend fun findBestTradeExactOut(
        networkTokenPair: NetworkTokenPair,
        amountOut: BigInt,
        dexEndpoints: List<DexEndpoint>? = null
    ): Pair<CalculatedSwapTrade.ExactOut?, List<SwapRouteState>> =
        findBestTrade(
            networkTokenPair = networkTokenPair,
            value = amountOut,
            type = SwapType.ExactOut,
            dexEndpoints = dexEndpoints ?: defaultDexEndpoints
        ).let { (first, second) -> first as CalculatedSwapTrade.ExactOut? to second }

    @Throws(Throwable::class)
    suspend fun findBestTradeExactIn(
        networkTokenPair: NetworkTokenPair,
        amountIn: BigInt,
        dexEndpoints: List<DexEndpoint>? = null
    ): Pair<CalculatedSwapTrade.ExactIn?, List<SwapRouteState>> =
        findBestTrade(
            networkTokenPair = networkTokenPair,
            value = amountIn,
            type = SwapType.ExactIn,
            dexEndpoints = dexEndpoints ?: defaultDexEndpoints
        ).let { (first, second) -> first as CalculatedSwapTrade.ExactIn? to second }

    /**
     * This method searches for the best swap variant between [networkTokenPair]
     *
     * @param value when [type] is a [SwapType.ExactIn] then it is the input amount,
     *  otherwise it is the output amount
     */
    @Throws(Throwable::class)
    suspend fun findBestTrade(
        networkTokenPair: NetworkTokenPair,
        value: BigInt,
        type: SwapType,
        dexEndpoints: List<DexEndpoint>? = null
    ): Pair<CalculatedSwapTrade?, List<SwapRouteState>> {
        require(networkTokenPair.network.chainId == network.chainId)
        require(networkTokenPair.nativesTokenCount <= 1 /*.count { it is NativeToken } <= 1 */) { "You cannot have two native tokens in on-chain swap" }

        val routes = SwapRoutesGenerator.getRoutes(
            networkPair = networkTokenPair,
            dexEndpoints = dexEndpoints ?: defaultDexEndpoints
        )

        val trades = when(type) {
            SwapType.ExactIn -> routes.map { (dex, route) -> dex to SwapTrade.ExactIn(value, route) }
            SwapType.ExactOut -> routes.map { (dex, route) -> dex to SwapTrade.ExactOut(value, route) }
        }

        return SwapTradesCalculator.calculate(
            client = executor,
            reservesRequestsFactory = reservesRequestsFactory,
            trades = trades
        ).let { (first, second) -> first.maxOrNull() to second }
    }

    @Throws(Throwable::class)
    fun findBestTradeExactOutCached(
        routes: List<SwapRouteState>,
        amountOut: BigInt
    ): CalculatedSwapTrade.ExactOut =
        findBestTradeCached(routes, amountOut, SwapType.ExactOut) as CalculatedSwapTrade.ExactOut

    @Throws(Throwable::class)
    fun findBestTradeExactInCached(
        routes: List<SwapRouteState>,
        amountIn: BigInt
    ): CalculatedSwapTrade.ExactIn =
        findBestTradeCached(routes, amountIn, SwapType.ExactIn) as CalculatedSwapTrade.ExactIn

    @Throws(Throwable::class)
    fun findBestTradeCached(
        routes: List<SwapRouteState>,
        value: BigInt,
        type: SwapType
    ): CalculatedSwapTrade {
        val trades = when(type) {
            SwapType.ExactIn -> routes.map { route -> SwapTradeState.ExactIn(value, route) }
            SwapType.ExactOut -> routes.map { route -> SwapTradeState.ExactOut(value, route) }
        }
        return SwapTradesCalculator.calculate(trades).maxOrNull()!!
    }

    @Throws(Throwable::class)
    suspend fun execute(
        credentials: Credentials,
        trade: CalculatedSwapTrade.Success,
        slippage: BigNum = "0.07".bn,
        deadline: BigInt = (timeMillis + 20.minutesAsMillis).bi,
    ): TransactionHash {
        require(slippage < 1.bn && slippage >= 0.bn) { "Tolerance should be in [0;1) range but was $slippage" }

        val path = trade.route.value
            .map(Token::thisOrWrapped)
            .map(Erc20Token::tokenAddress)

        return when {
            trade.route.value.first() is Erc20Token && trade.route.value.last() is Erc20Token -> when (trade) {
                is CalculatedSwapTrade.ExactIn -> router.swapExactTokensForTokens(
                    credentials = credentials,
                    amountIn = trade.amountIn,
                    amountOutMin = trade.amountOutMin(slippage),
                    path = path,
                    deadline = deadline
                )
                is CalculatedSwapTrade.ExactOut.Success -> router.swapTokensForExactTokens(
                    credentials = credentials,
                    amountOut = trade.amountOut,
                    amountInMax = trade.amountInMax(slippage),
                    path = path,
                    deadline = deadline,
                )
            }
            else  -> when (trade) {
                is CalculatedSwapTrade.ExactIn -> when {
                    trade.route.value.first() is NativeToken -> router.swapExactNativeForTokens(
                        credentials = credentials,
                        amountInNative = trade.amountIn,
                        amountOutMin = trade.amountOutMin(slippage),
                        path = path,
                        deadline = deadline
                    )
                    else -> router.swapExactTokensForNative(
                        credentials = credentials,
                        amountIn = trade.amountIn,
                        amountOutMin = trade.amountOutMin(slippage),
                        path = path,
                        deadline = deadline
                    )
                }
                is CalculatedSwapTrade.ExactOut.Success -> when {
                    trade.route.value.last() is NativeToken -> router.swapTokensForExactNative(
                        credentials = credentials,
                        amountOutNative = trade.amountOut,
                        amountInMax = trade.amountInMax(slippage),
                        path = path,
                        deadline = deadline,
                    )
                    else -> router.swapNativeForExactTokens(
                        credentials = credentials,
                        amountOut = trade.amountOut,
                        amountInMaxNative = trade.amountInMax(slippage),
                        path = path,
                        deadline = deadline
                    )
                }
            }
        }
    }
}
