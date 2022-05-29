package com.symbiosis.sdk.swap.crosschain

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.ClientsManager
import com.symbiosis.sdk.currency.DecimalsToken
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.currency.Token
import com.symbiosis.sdk.currency.TokenAmount
import com.symbiosis.sdk.currency.TokenPair
import com.symbiosis.sdk.currency.amount
import com.symbiosis.sdk.currency.convertRealToInteger
import com.symbiosis.sdk.swap.Percentage
import com.symbiosis.sdk.swap.crosschain.fromToken
import com.symbiosis.sdk.swap.uni.UniLikeSwapRepository
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.WalletAddress

@RequiresOptIn(
    message = "Please, consider to use ClientsManager.getCrossChainClient instead of the raw constructor, because " +
            "it's signature may be changed in the future.",
    level = RequiresOptIn.Level.WARNING
)
annotation class RawUsageOfCrossChainConstructor

class CrossChainClient @RawUsageOfCrossChainConstructor constructor(val crossChain: CrossChain) {
    private val firstNetworkClient = ClientsManager.getNetworkClient(crossChain.fromNetwork)
    private val repository = CrossChainSwapRepository(crossChain)


    init {
        require(crossChain.fromNetwork.chainId != crossChain.toNetwork.chainId) {
            "CrossChain swap should be from different networks"
        }
    }

    sealed interface AllowedRangeResult {
        class Success(val minAmount: BigInt, val maxAmount: BigInt) : AllowedRangeResult
        object TradeNotFound : AllowedRangeResult
    }

    @Throws(Throwable::class)
    suspend fun getAllowedRawRangeForInput(
        fromToken: Token,
        slippageTolerance: Percentage = Percentage(0.07.bn),
    ): AllowedRangeResult {
        require(fromToken.network.chainId == crossChain.fromNetwork.chainId) {
            "fromToken is from invalid network (${fromToken.network.networkName}, but required ${crossChain.fromNetwork.networkName})"
        }

        val minTradeResult = firstNetworkClient.uniLike.exactOut(
            tokens = NetworkTokenPair(
                first = fromToken,
                second = crossChain.fromToken
            ),
            amountOut = crossChain.fromToken.convertRealToInteger(crossChain.minStableTokensAmountPerTrade),
        )

        val minTrade = when (minTradeResult) {
            is UniLikeSwapRepository.ExactOutResult.Success -> minTradeResult.trade
            UniLikeSwapRepository.ExactOutResult.TradeNotFound,
            UniLikeSwapRepository.ExactOutResult.InsufficientLiquidity ->
                return AllowedRangeResult.TradeNotFound
        }

        val maxTradeResult = minTrade.route.exactOut(
            amountOut = crossChain
                .fromToken
                .amount(crossChain.maxStableTokensAmountPerTrade)
                .raw
        )

        val maxTrade = when (maxTradeResult) {
            is UniLikeSwapRepository.CalculatedRoute.ExactOutResult.Success ->
                maxTradeResult.trade
            UniLikeSwapRepository.CalculatedRoute.ExactOutResult.InsufficientLiquidity ->
                return AllowedRangeResult.TradeNotFound
        }

        return AllowedRangeResult.Success(
            minAmount = minTrade.amountInMax(slippageTolerance),
            maxAmount = maxTrade.amountInMax(slippageTolerance)
        )
    }

    sealed interface DecimalsAllowedRangeResult {
        class Success(val minAmount: TokenAmount, val maxAmount: TokenAmount) : DecimalsAllowedRangeResult
        object TradeNotFound : DecimalsAllowedRangeResult
    }

    @Throws(Throwable::class)
    suspend fun getAllowedRangeForInput(
        fromToken: DecimalsToken,
        slippageTolerance: Percentage = Percentage(0.07.bn),
    ): DecimalsAllowedRangeResult =
        when (val result = getAllowedRawRangeForInput(fromToken as Token, slippageTolerance)) {
            is AllowedRangeResult.Success ->
                DecimalsAllowedRangeResult.Success(
                    minAmount = fromToken.amount(result.minAmount),
                    maxAmount = fromToken.amount(result.maxAmount)
                )
            AllowedRangeResult.TradeNotFound ->
                DecimalsAllowedRangeResult.TradeNotFound
        }

    /**
     * find Best trade using meta router
     * @param fromToken token we want to swap from
     * @param targetToken token we want to receive
     * @param amountIn desired amount of fromToken
     * @param slippageTolerance default valued tolerance
     **/
    @Throws(Throwable::class)
    suspend fun findBestTradeExactIn(
        from: WalletAddress,
        tokens: TokenPair,
        amountIn: BigInt,
        slippageTolerance: Percentage = Percentage(0.07.bn),
        recipient: EthereumAddress = from
    ): CrossChainSwapRepository.Result {
        require(slippageTolerance <= 50 && slippageTolerance >= 0) { "Tolerance should be in [0;1) range but was $slippageTolerance" }

        require(tokens.first.network.chainId == crossChain.fromNetwork.chainId) {
            "fromToken is from invalid network (${tokens.first.network.networkName}, but required ${crossChain.fromNetwork.networkName})"
        }
        require(tokens.second.network.chainId == crossChain.toNetwork.chainId) {
            "targetToken is from invalid network (${tokens.second.network.networkName}, but required ${crossChain.toNetwork.networkName})"
        }

        return repository.findBestTradeExactIn(amountIn, tokens, slippageTolerance, from, recipient)
    }
}
