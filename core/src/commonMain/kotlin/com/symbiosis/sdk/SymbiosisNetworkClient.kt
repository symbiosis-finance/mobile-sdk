package com.symbiosis.sdk

import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.stuck.StuckTransaction
import com.symbiosis.sdk.stuck.StuckTransactionsRepository
import com.symbiosis.sdk.swap.oneInch.OneInchSwapRepository
import com.symbiosis.sdk.swap.singleNetwork.SingleNetworkSwapRepository
import com.symbiosis.sdk.swap.uni.UniLikeSwapRepository
import dev.icerock.moko.web3.entity.WalletAddress

/**
 * Everything for swaps inside one network
 */
class SymbiosisNetworkClient(val networkClient: NetworkClient) {
    val uniLike: UniLikeSwapRepository = UniLikeSwapRepository(networkClient)
    val oneInchIfSupported: OneInchSwapRepository? = OneInchSwapRepository(networkClient)
    val swap: SingleNetworkSwapRepository = SingleNetworkSwapRepository(uniLike, oneInchIfSupported)

    suspend fun getStuckTransactions(
        address: WalletAddress,
        otherClients: List<SymbiosisNetworkClient>,
        advisorUrl: String
    ): List<StuckTransaction> =
        StuckTransactionsRepository(mainClient = this, otherClients, advisorUrl)
            .getStuckTransactions(address)

}

val Network.symbiosisClient get() = SymbiosisNetworkClient(NetworkClient(network = this))
