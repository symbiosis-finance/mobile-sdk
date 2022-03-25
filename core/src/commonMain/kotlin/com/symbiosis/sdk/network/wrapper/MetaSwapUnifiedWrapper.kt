package com.symbiosis.sdk.network.wrapper

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.configuration.StablePoolProvider
import com.symbiosis.sdk.configuration.SwapTTLProvider
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.network.contract.NerveContract
import com.symbiosis.sdk.network.contract.provideDeadline
import com.symbiosis.sdk.swap.TokensType
import com.symbiosis.sdk.swap.meta.CalculatedMetaSwapTrade
import com.symbiosis.sdk.swap.meta.NerveStablePool
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.TransactionHash

abstract class MetaSwapUnifiedWrapper(
    private val network: Network,
    private val defaultStablePoolProvider: StablePoolProvider,
    private val targetNetwork: Network,
    private val defaultSwapTTLProvider: SwapTTLProvider,
    private val nerveProvider: (NerveStablePool) -> NerveContract,
) {
    private suspend fun deadline() = provideDeadline(defaultSwapTTLProvider.getSwapTTL(network.chainId))

    /**
     * Method to provide execution of strict meta router
     * @param credentials wallet credentials
     * @param trade calculated best trade
     * @param tokensType current type of trade (has native tokens in path or not and where)
     */
    abstract suspend fun execute(
        credentials: Credentials,
        trade: CalculatedMetaSwapTrade.ExactIn,
        tokensType: TokensType.CrossChainTokensType,
        deadline: BigInt? = null,
        stablePoolProvider: StablePoolProvider? = null
    ): TransactionHash

    protected suspend fun getCurrentNerveStablePool(
        nerveStablePoolProvider: StablePoolProvider?
    ): NerveStablePool = nerveStablePoolProvider
        ?.getStablePool(network.chainId, targetNetwork.chainId)
        ?: defaultStablePoolProvider.getStablePool(network.chainId, targetNetwork.chainId)
}
