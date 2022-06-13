package com.symbiosis.sdk

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.currency.DecimalsErc20Token
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.swap.crosschain.SymbiosisCrossChainClient
import com.symbiosis.sdk.swap.unified.UnifiedSwapRepository
import dev.icerock.moko.web3.ContractAddress

/**
 * This is a parent class for all swap clients
 */
interface ClientsManager {
    val allNetworks: List<Network>
    val allTokens: List<DecimalsToken>
    val allClients: List<SymbiosisNetworkClient>
    val allCrossChainClients: List<SymbiosisCrossChainClient>

    val swap: UnifiedSwapRepository get() = UnifiedSwapRepository(allCrossChainClients.map { it.crossChain })
}

fun ClientsManager.findToken(address: ContractAddress, chainId: BigInt): DecimalsErc20Token? =
    allTokens.filterIsInstance<DecimalsErc20Token>()
        .find { token -> token.network.chainId == chainId && token.tokenAddress == address }

fun ClientsManager.getCrossChainClient(firstNetwork: Network, secondNetwork: Network) =
    getCrossChainClient(firstNetwork.chainId, secondNetwork.chainId)

fun ClientsManager.getCrossChainClient(firstNetworkChainId: BigInt, secondNetworkChainId: BigInt) = allCrossChainClients
    .find { it.crossChain.fromNetwork.chainId == firstNetworkChainId &&
            it.crossChain.toNetwork.chainId == secondNetworkChainId }
