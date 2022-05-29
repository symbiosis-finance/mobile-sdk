package com.symbiosis.sdk.swap.uni

import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.network.NetworkClient

class Web3UniLikeSwapRouter(private val networkClient: NetworkClient) : UniLikeSwapRepository.Router {
    override fun buildRoutes(tokens: NetworkTokenPair): UniLikeSwapRepository.Routes {
        val routes = UniLikeSwapRoutesGenerator
            .getRoutes(tokens)

        return Web3UniLikeSwapRoutes(networkClient, routes)
    }
}
