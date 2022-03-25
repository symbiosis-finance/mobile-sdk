package com.symbiosis.sdk.swap

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.dex.DexEndpoint
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.Web3RpcRequest
import kotlinx.serialization.json.JsonElement

object SwapTradesCalculator {
    fun interface ReservesRequestsFactory {
        fun getReservesRequest(dex: DexEndpoint, pair: NetworkTokenPair): Web3RpcRequest<JsonElement, Reserves>
    }

    private fun prepareReservesRequests(
        reservesRequestsFactory: ReservesRequestsFactory,
        routes: List<Pair<DexEndpoint, SwapRoute>>
    ): List<List<Web3RpcRequest<JsonElement, Reserves>>> =
        routes.map { (dex, route) ->
            route.pairs.map { pair ->
                reservesRequestsFactory
                    .getReservesRequest(dex, pair)
            }
        }

    private suspend fun getRoutesState(
        client: Web3Executor,
        reservesRequestsFactory: ReservesRequestsFactory,
        routes: List<Pair<DexEndpoint, SwapRoute>>
    ): List<SwapRouteState?> {
        val reservesRequests = prepareReservesRequests(
            reservesRequestsFactory = reservesRequestsFactory,
            routes = routes
        )

        val reservesRaw = client.executeBatch(reservesRequests.flatten())

        val lengths = reservesRequests.runningFold(initial = 0) { acc, list -> acc + list.size }

        return reservesRequests
            .mapIndexed { offset, route ->
                route
                    .mapIndexed { index, _ -> reservesRaw[lengths[offset] + index] }
            }
            .zip(routes)
            .map resultMap@{ (reservesList, route) ->
                val reservesDataList = reservesList.map { it as? ReservesData ?: return@resultMap null }
                return@resultMap route.second.pairs
                    .zip(reservesDataList)
                    .map { (pair, reserves) -> Triple(pair, route.first, reserves) }
                    .asRouteState()
            }.also { println("reserves: $it") }
    }

    private fun calculateTaxes(amountIn: BigInt, route: SwapRouteState, liquidityProviderFeePercent: BigNum): BigInt {
        val multiplier = 1.bn - liquidityProviderFeePercent
        val taxPercent = 1.bn - route.route.pairs.fold(initial = 1.bn) { acc, _ -> acc * multiplier }
        val bn = amountIn.toBigNum() * taxPercent
        return bn.convertToScale(otherScale = 0).int
    }

    /**
     * @return value, mid-value (no impact), commission. may be null only if the current trade is exact out
     */
    private fun SwapTradeState.getTargetValue(liquidityProviderFeePercent: BigNum): Triple<BigInt, BigInt, BigInt>? {
        return when(this) {
            is SwapTradeState.ExactIn -> {
                val taxes = calculateTaxes(amountIn, route, liquidityProviderFeePercent)
                fun price(hasImpact: Boolean) = calculateOutPrice(
                    route = route,
                    amountIn = amountIn - taxes,
                    hasImpact = hasImpact
                )

                Triple(price(hasImpact = true), price(hasImpact = false), taxes)
            }
            is SwapTradeState.ExactOut -> {
                fun price(hasImpact: Boolean): Pair<BigInt, BigInt>? {
                    val withoutTaxes = calculateInPrice(
                        route = route,
                        amountOut = amountOut,
                        hasImpact = hasImpact
                    ) ?: return null
                    val taxes = calculateTaxes(withoutTaxes, route, liquidityProviderFeePercent)
                    return (withoutTaxes + taxes) to taxes
                }

                val (priceHasImpact, taxes) = price(hasImpact = true) ?: return null
                val (priceNoImpact, _) = price(hasImpact = false) ?: return null

                Triple(priceHasImpact, priceNoImpact, taxes)
            }
        }
    }

    private fun calculateOutPrice(route: SwapRouteState, amountIn: BigInt, hasImpact: Boolean): BigInt =
        route.value.also { println("route: $it") }
            .fold(initial = amountIn.toBigNum()) { acc, pool ->
                val newReserves = when(hasImpact) {
                    true -> pool.reserves.copy(reserve1 = pool.reserves.reserve1 + acc.bi)
                    false -> pool.reserves
                }
                return@fold acc.div(newReserves.price2, precision = 18)
            }.bi

    private fun calculateInPrice(route: SwapRouteState, amountOut: BigInt, hasImpact: Boolean): BigInt? {
        return route.value.asReversed()
            .fold(initial = amountOut.toBigNum()) { acc, pool ->
                val newReserves = when(hasImpact) {
                    true -> {
                        val newReserve2 = (pool.reserves.reserve2 - acc.bi)
                            .takeIf { it > 0.bi } ?: return null
                        pool.reserves.copy(reserve2 = newReserve2)
                    }
                    false -> pool.reserves
                }
                return@fold acc.div(newReserves.price1, precision = 18)
            }.bi
    }

    private fun SwapTradeState.calculate(): CalculatedSwapTrade {
        val (targetValue, targetValueNoImpact, taxes) = getTargetValue(route.dex.liquidityProviderFeePercent)
            ?: return CalculatedSwapTrade.ExactOut.InsufficientLiquidity

        return when(this) {
            is SwapTradeState.ExactIn -> CalculatedSwapTrade.ExactIn(
                amountIn = value,
                amountOut = targetValue,
                liquidityProviderFee = taxes,
                priceImpact = 1.bn - targetValue.toBigNum().div(targetValueNoImpact.toBigNum(), precision = 18),
                route = route.route,
                dex = route.dex
            )
            is SwapTradeState.ExactOut -> CalculatedSwapTrade.ExactOut.Success(
                amountIn = targetValue,
                amountOut = value,
                liquidityProviderFee = taxes,
                priceImpact = 1.bn - targetValueNoImpact.toBigNum().div(targetValue.toBigNum(), precision = 18),
                route = route.route,
                dex = route.dex
            )
        }
    }

    suspend fun calculate(
        client: Web3Executor,
        reservesRequestsFactory: ReservesRequestsFactory,
        trades: List<Pair<DexEndpoint, SwapTrade>>
    ): Pair<List<CalculatedSwapTrade>, List<SwapRouteState>> {
        val routesState = getRoutesState(
            client = client,
            reservesRequestsFactory = reservesRequestsFactory,
            routes = trades.map { (dex, trade) -> dex to trade.route }
        )
        val tradesState = routesState
            .zip(trades.map { it.second })
            .mapNotNull { (route, trade) ->
                route ?: return@mapNotNull null
                when(trade) {
                    is SwapTrade.ExactIn -> SwapTradeState.ExactIn(trade.amountIn, route)
                    is SwapTrade.ExactOut -> SwapTradeState.ExactOut(trade.amountOut, route)
                }
            }
        return calculate(tradesState) to routesState.filterNotNull()
    }

    fun calculate(
        trades: List<SwapTradeState>,
    ): List<CalculatedSwapTrade> = trades.map { it.calculate() }
}

private val BigNum.bi get() = convertToScale(otherScale = 0).int
