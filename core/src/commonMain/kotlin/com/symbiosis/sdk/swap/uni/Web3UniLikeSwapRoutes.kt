package com.symbiosis.sdk.swap.uni

import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.NetworkClient
import dev.icerock.moko.web3.entity.ContractAddress

class Web3UniLikeSwapRoutes(
  private val networkClient: NetworkClient,
  private val routes: List<Route>
) : UniLikeSwapRepository.Routes {

    override suspend fun fetch(): UniLikeSwapRepository.CalculatedRoutes {
        val requests = prepareRequests()
        val reservesRaw = networkClient.executeBatch(requests.map { (_, request) -> request })
        val calculatedRoutes = zipRoutes(requests.map { (address) -> address }, reservesRaw)
        return Web3UniLikeSwapCalculatedRoutes(networkClient, calculatedRoutes)
    }

    private fun prepareRequests() = routes
        .flatMap { (dex, pairs) ->
            pairs.map { pair ->
                networkClient
                    .getPoolContract(dex, pair)
            }.map { pool ->
                pool.address to pool.getReservesRequest()
            }
        }

    private fun zipRoutes(
        addresses: List<ContractAddress>,
        reserves: List<Reserves>
    ): List<Web3UniLikeSwapCalculatedRoute> {
        val reservesIterator = reserves.iterator()
        val addressesIterator = addresses.iterator()

        val routes = routes.mapNotNull route@ { route ->
            val pairs = route.pairs
                .map { pair ->
                    val address = addressesIterator.next()
                    when (val reservesData = reservesIterator.next()) {
                        is Reserves.Empty -> return@route null
                        is ReservesData -> UniLikePool(pair, reservesData, address)
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
