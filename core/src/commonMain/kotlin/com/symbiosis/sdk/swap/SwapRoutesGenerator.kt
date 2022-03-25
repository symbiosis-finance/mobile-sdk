package com.symbiosis.sdk.swap

import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.Network

object SwapRoutesGenerator {

    fun getBaseRoutes(network: Network, maxHops: Int = 3): List<SwapRoute> {
        require(maxHops > 0)
        if (maxHops == 1)
            return listOf(SwapRoute(value = network.swapBases))

        val previousRoutes = getBaseRoutes(network, maxHops = maxHops - 1)

        val nextRoutes = previousRoutes.flatMap { previousRoute ->
            network.swapBases
                .filter { currency -> currency.network == network && currency !in previousRoute.value }
                .map { currency -> SwapRoute(value = previousRoute.value + currency) }
                .filter { route -> route !in previousRoutes }
        }

        return previousRoutes + nextRoutes
    }

    fun getRoutes(
        networkPair: NetworkTokenPair,
        dexEndpoints: List<DexEndpoint>,
        maxHops: Int = 3
    ): List<Pair<DexEndpoint, SwapRoute>> {
        val routes = getBaseRoutes(
            network = networkPair.network,
            maxHops = maxHops
        ).filter { route ->
            networkPair.first !in route.value && networkPair.second !in route.value
        }.map { baseRoute ->
            SwapRoute(value = listOf(networkPair.first) + baseRoute.value + networkPair.second)
        } + SwapRoute(value = listOf(networkPair.first, networkPair.second))

        return dexEndpoints.flatMap { dex ->
            routes.map { route -> dex to route }
        }
    }
}
