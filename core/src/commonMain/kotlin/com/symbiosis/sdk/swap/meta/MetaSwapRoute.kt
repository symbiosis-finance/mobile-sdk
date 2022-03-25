package com.symbiosis.sdk.swap.meta

import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.thisOrWrapped
import com.symbiosis.sdk.network.contract.SynthFabricContract
import com.symbiosis.sdk.network.contract.getSyntheticToken
import com.symbiosis.sdk.swap.CalculatedSwapTrade
import dev.icerock.moko.web3.ContractAddress

/**
 * @param pathToStable the best swap trade on first network where the last element is the stable one
 * @param pathFromStable the best swap trade on second network where the first element is the stable one
 */
data class MetaSwapRoute(
    val pathToStable: CalculatedSwapTrade.ExactIn?,
    val firstNetworkStableToken: DecimalsErc20Token,
    val lastNetworkStableToken: DecimalsErc20Token,
    val pathFromStable: CalculatedSwapTrade.ExactIn?,
    val stablePoolLocation: StablePoolLocation,
    val nerveAddress: ContractAddress
) {
    constructor(
        pathToStable: CalculatedSwapTrade.ExactIn,
        firstNetworkStableTokenDecimals: Int,
        lastNetworkStableTokenDecimals: Int,
        pathFromStable: CalculatedSwapTrade.ExactIn,
        stablePoolLocation: StablePoolLocation,
        nerveAddress: ContractAddress
    ) : this(
        pathToStable = pathToStable,
        firstNetworkStableToken = pathToStable.route.value.last().thisOrWrapped
            .let { DecimalsErc20Token(it.network, it.tokenAddress, firstNetworkStableTokenDecimals) },
        lastNetworkStableToken = pathFromStable.route.value.first().thisOrWrapped
            .let { DecimalsErc20Token(it.network, it.tokenAddress, lastNetworkStableTokenDecimals) },
        pathFromStable = pathFromStable,
        stablePoolLocation = stablePoolLocation,
        nerveAddress = nerveAddress
    )

    init {
        require(pathToStable == null ||
                pathToStable.route.value.last().thisOrWrapped.tokenAddress == firstNetworkStableToken.tokenAddress) {
            "Last token of path to stable should be same as first token of stable pool"
        }
        require(pathFromStable == null ||
                pathFromStable.route.value.first().thisOrWrapped.tokenAddress == lastNetworkStableToken.tokenAddress) {
            "First token of path from stable should be same as second token of stable pool"
        }
    }

    val fromNetwork get() = firstNetworkStableToken.network
    val toNetwork get() = lastNetworkStableToken.network

    // Automatically choose the correct network client for function below
    suspend fun getStablePoolPath(
        firstNetworkSynthFabric: SynthFabricContract,
        secondNetworkSynthFabric: SynthFabricContract
    ): NetworkTokenPair = when (stablePoolLocation) {
        StablePoolLocation.FirstNetwork -> getStablePoolPath(firstNetworkSynthFabric)
        StablePoolLocation.LastNetwork -> getStablePoolPath(secondNetworkSynthFabric)
    }

    suspend fun getStablePoolPath(synthFabric: SynthFabricContract): NetworkTokenPair =
        when (stablePoolLocation) {
            StablePoolLocation.FirstNetwork -> {
                require(synthFabric.network.chainId == fromNetwork.chainId) {
                    "You should provide the client on first network in case the stable pool is in the first one"
                }
                NetworkTokenPair(
                    first = firstNetworkStableToken,
                    second = synthFabric.getSyntheticToken(lastNetworkStableToken)
                        ?: error("Synthetic pair was not found for ${lastNetworkStableToken.tokenAddress}")
                )
            }
            StablePoolLocation.LastNetwork -> {
                require(synthFabric.network.chainId == toNetwork.chainId) {
                    "You should provide the client on second network in case the stable pool is in the second one"
                }
                NetworkTokenPair(
                    first = synthFabric.getSyntheticToken(firstNetworkStableToken)
                        ?: error("Synthetic pair was not found for ${firstNetworkStableToken.tokenAddress}"),
                    second = lastNetworkStableToken
                )
            }
        }

    enum class StablePoolLocation {
        FirstNetwork,
        LastNetwork
    }
}
