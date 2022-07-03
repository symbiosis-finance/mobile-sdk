package com.symbiosis.sdk.swap.uni

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.currency.NetworkTokenPair
import com.symbiosis.sdk.internal.kbignum.toBigNum
import dev.icerock.moko.web3.entity.ContractAddress

data class UniLikePool(val pair: NetworkTokenPair.Erc20Only, val reserves: ReservesData, val address: ContractAddress) {
    fun outputAmountWithoutFee(amountIn: BigInt, hasImpact: Boolean): BigInt {
        val newReserves = when (hasImpact) {
            true -> reserves.copy(reserve1 = reserves.reserve1 + amountIn)
            false -> reserves
        }
        return amountIn.toBigNum().div(newReserves.price2, precision = 18).toBigInt()
    }

    sealed interface InputAmountResult {
        object InsufficientLiquidity : InputAmountResult
        class Success(val amountIn: BigInt) : InputAmountResult
    }

    // without impact there is no way to get insufficient liquidity error
    fun inputAmountWithoutFee(amountOut: BigInt, hasImpact: Boolean): InputAmountResult {
        val newReserves = when (hasImpact) {
            true -> {
                val newReserve2 = reserves.reserve2 - amountOut
                if (newReserve2 <= 0.bi) return InputAmountResult.InsufficientLiquidity
                reserves.copy(reserve2 = newReserve2)
            }
            false -> reserves
        }
        val amountIn = amountOut.toBigNum().div(newReserves.price1, precision = 18).toBigInt()
        return InputAmountResult.Success(amountIn)
    }

    override fun toString(): String = "UniLikePool(address=$address, route=${pair.first} -> ${pair.second}, reserves=$reserves)"
}

fun List<UniLikePool>.amountOutWithoutFee(amountIn: BigInt, hasImpact: Boolean) =
    fold(initial = amountIn) { currentAmountIn, pool ->
        pool.outputAmountWithoutFee(currentAmountIn, hasImpact)
    }

sealed interface AmountInWithoutFeeResult {
    object InsufficientLiquidity : AmountInWithoutFeeResult
    class Success(val amountIn: BigInt) : AmountInWithoutFeeResult
}

// without impact there is no way to get insufficient liquidity error
fun List<UniLikePool>.amountInWithoutFee(amountOut: BigInt, hasImpact: Boolean): AmountInWithoutFeeResult {
    return asReversed().fold(initial = AmountInWithoutFeeResult.Success(amountOut)) { currentAmountInResult, pool ->
        when (val amountInResult = pool.inputAmountWithoutFee(currentAmountInResult.amountIn, hasImpact)) {
            is UniLikePool.InputAmountResult.InsufficientLiquidity ->
                return AmountInWithoutFeeResult.InsufficientLiquidity
            is UniLikePool.InputAmountResult.Success ->
                AmountInWithoutFeeResult.Success(amountInResult.amountIn)
        }
    }
}
