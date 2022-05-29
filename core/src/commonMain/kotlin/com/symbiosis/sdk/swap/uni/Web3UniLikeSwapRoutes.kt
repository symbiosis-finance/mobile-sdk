package com.symbiosis.sdk.swap.uni

import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.NetworkClient

class Web3UniLikeSwapRoutes(
  private val networkClient: NetworkClient,
  private val routes: List<Route>
) : UniLikeSwapRepository.Routes {

    override suspend fun fetch(): UniLikeSwapRepository.CalculatedRoutes {
        val requests = prepareRequests()
        val reservesRaw = networkClient.executeBatch(requests)
        val calculatedRoutes = zipRoutes(reservesRaw)
        return Web3UniLikeSwapCalculatedRoutes(networkClient, calculatedRoutes)
    }

    private fun prepareRequests() = routes
        .flatMap { (dex, pairs) ->
            pairs.map { pair ->
                networkClient
                    .getPoolContract(dex, pair)
                    .getReservesRequest()
            }
        }

    private fun zipRoutes(reserves: List<Reserves>): List<Web3UniLikeSwapCalculatedRoute> {
        val iterator = reserves.iterator()

        val routes = routes.mapNotNull route@ { route ->
            val pairs = route.pairs
                .map { pair ->
                    when (val reservesData = iterator.next()) {
                        is Reserves.Empty -> return@route null
                        is ReservesData -> UniLikePool(pair, reservesData)
                    }
                }
            return@route Web3UniLikeSwapCalculatedRoute(networkClient, route.dexEndpoint, pairs, route.pair)
        }

        return routes
    }

    data class Route(
        val dexEndpoint: DexEndpoint,
        val pairs: List<NetworkTokenPair.Erc20Only>,
        val pair: NetworkTokenPair
    )
}
