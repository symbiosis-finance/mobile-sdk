package com.symbiosis.sdk.crosschain

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.currency.Erc20Token
import com.symbiosis.sdk.network.contract.metaRouter.MetaRouterV2Contract
import com.symbiosis.sdk.swap.meta.NerveStablePool
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.hex.HexString

// Direction-dependent data moved into this interface
interface CrossChainSwapDirection {
    fun provideStableSwapCallData(pool: NerveStablePool, amountIn: BigInt, deadline: BigInt): HexString
    suspend fun getOtherSideCallData(
        stableBridgingFee: BigInt,
        stablePoolAmountIn: BigInt,
        stablePoolAmountOut: BigInt,
        fromAddress: WalletAddress,
        finalDexRouter: ContractAddress,
        stableSwapCallData: HexString,
        finalSwapCallData: HexString?,
        oppositeBridge: ContractAddress,
        finalNetworkChainId: BigInt,
        stablePool: NerveStablePool,
        stablePoolRoute: List<ContractAddress>
    ): HexString
    suspend fun getStablePoolRoute(pool: NerveStablePool): List<ContractAddress>
    suspend fun metaRoute(
        credentials: Credentials,
        firstToken: MetaRouterV2Contract.FirstToken,
        stablePool: NerveStablePool,
        stablePoolRoute: List<ContractAddress>,
        firstNetworkChainId: BigInt,
        firstSwapCallData: HexString?,
        stableSwapCallData: HexString,
        firstDexRouter: ContractAddress,
        stableDexRouter: ContractAddress,
        amountIn: BigInt,
        otherSideCallData: HexString,
        gasProvider: GasProvider
    ): TransactionHash

    suspend fun buildBridgeCallData(
        firstSwapAmountOut: BigInt,
        stableSwapRoute: List<ContractAddress>,
        // the difference between this and the parameter above is
        // that the current parameter contains 2 real tokens
        // while the one above contains one synthetic token
        stablePoolTokens: List<Erc20Token>,
        stableSwapCallData: HexString,
        stablePoolAddress: ContractAddress,
        finalSwapAmountIn: BigInt,
        to: WalletAddress,
        finalSwapCallData: HexString?,
        finalRouterAddress: ContractAddress
    ): HexString
}
