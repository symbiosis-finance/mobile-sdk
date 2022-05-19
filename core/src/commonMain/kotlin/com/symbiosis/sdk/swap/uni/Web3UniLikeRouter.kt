package com.symbiosis.sdk.swap.uni

import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.swap.UniLikeSwapRoutesGenerator

class Web3UniLikeRouter(private val networkClient: NetworkClient) : UniLikeSwapCalculator.UniLikeRouter {
    override fun buildRoutes(pair: NetworkTokenPair): UniLikeSwapCalculator.Routes {
        val routes = UniLikeSwapRoutesGenerator
            .getRoutes(pair)

        return Web3UniLikeRoutes(networkClient, routes)
    }
}
