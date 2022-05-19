package com.symbiosis.sdk.swap.uni

import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.swap.Reserves
import com.symbiosis.sdk.swap.ReservesData

class Web3UniLikeRoutes(
    private val networkClient: NetworkClient,
    private val routes: List<Route>
) : UniLikeSwapCalculator.Routes {
    override suspend fun fetch(): List<UniLikeSwapCalculator.CalculatedRoute> {
        val requests = prepareRequests()
        val reservesRaw = networkClient.executeBatch(requests)
        return zipRoutes(reservesRaw)
    }

    private fun prepareRequests() = routes
        .flatMap { (dex, pairs) ->
            pairs.map { pair ->
                networkClient
                    .getPoolContract(dex, pair)
                    .getReservesRequest()
            }
        }

    private fun zipRoutes(reserves: List<Reserves>): List<Web3UniLikeCalculatedRoute> {
        val iterator = reserves.iterator()

        val routes = routes.mapNotNull route@ { route ->
            val pairs = route.pairs
                .map { pair ->
                    when (val reservesData = iterator.next()) {
                        is Reserves.Empty -> return@route null
                        is ReservesData -> Web3UniLikePool(pair, reservesData)
                    }
                }
            return@route Web3UniLikeCalculatedRoute(networkClient, route.dexEndpoint, pairs, route.pair)
        }

        return routes
    }

    data class Route(
        val dexEndpoint: DexEndpoint,
        val pairs: List<NetworkTokenPair.Erc20Only>,
        val pair: NetworkTokenPair
    )
}
