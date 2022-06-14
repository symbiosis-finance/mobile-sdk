package com.symbiosis.sdk.stuck

import com.symbiosis.sdk.SymbiosisNetworkClient

fun StuckTransactionsRepository(
    mainClient: SymbiosisNetworkClient,
    otherClients: List<SymbiosisNetworkClient>,
    advisorUrl: String
) = StuckTransactionsRepository(
    mainClient, advisorUrl,
    adapter = DefaultStuckTransactionsAdapter(
        mainClient.networkClient, otherClients.map(SymbiosisNetworkClient::networkClient)
    )
)
