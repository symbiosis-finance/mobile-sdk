package com.symbiosis.sdk.stuck

import com.symbiosis.sdk.ClientsManager
import com.symbiosis.sdk.network.NetworkClient
import dev.icerock.moko.web3.WalletAddress

suspend fun ClientsManager.getStuckTransactions(
    address: WalletAddress,
    clients: List<NetworkClient> = allClients
): List<StuckRequest> = clients.flatMap { it.getStuckTransactions(address, clients) }
