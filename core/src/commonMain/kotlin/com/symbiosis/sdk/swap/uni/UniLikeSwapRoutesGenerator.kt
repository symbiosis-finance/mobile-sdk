package com.symbiosis.sdk.swap.uni

import com.symbiosis.sdk.swap.uni.Web3UniLikeSwapRoutes.Route
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.dex.DexEndpoint
import com.symbiosis.sdk.network.Network

typealias SwapRoute = List<Erc20Token>

object UniLikeSwapRoutesGenerator {
    fun getBaseRoutes(network: Network, maxHops: Int = 3): List<SwapRoute> {
        require(maxHops > 0)

        if (maxHops == 1) return network.swapBases.map { listOf(it) }

        val previousRoutes = getBaseRoutes(network, maxHops = maxHops - 1)

        val nextRoutes = previousRoutes.flatMap { previousRoute ->
            network.swapBases
                .filter { currency -> currency !in previousRoute }
                .map { currency -> previousRoute + currency }
                .filter { route -> route !in previousRoutes }
        }

        return previousRoutes + nextRoutes
    }

    fun getRoutes(
        networkPair: NetworkTokenPair,
        dexEndpoints: List<DexEndpoint> = networkPair.network.dexEndpoints,
        maxHops: Int = 3
    ): List<Route> {
        val wrappedPair: NetworkTokenPair.Erc20Only = networkPair.thisOrWrapped

        val routes: List<SwapRoute> = getBaseRoutes(
            network = networkPair.network,
            maxHops = maxHops
        ).filter { route ->
            wrappedPair.first !in route && wrappedPair.second !in route
        }.map { baseRoute ->
            listOf(wrappedPair.first) + baseRoute + wrappedPair.second
        } + listOf(listOf(wrappedPair.first, wrappedPair.second))

        val pairs: List<List<NetworkTokenPair.Erc20Only>> = routes.map { route ->
            route
                .windowed(size = 2) { (first, second) ->
                    NetworkTokenPair.Erc20Only(first, second)
                }
        }

        return dexEndpoints.flatMap { dexEndpoint ->
            pairs.map { currentPairs ->
                return@map Route(dexEndpoint, currentPairs, networkPair)
            }
        }
    }
}
