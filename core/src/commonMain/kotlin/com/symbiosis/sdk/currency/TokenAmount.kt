package com.symbiosis.sdk.currency

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum

@JvmInline
value class TokenAmount private constructor(val amount: BigNum) {
    constructor(raw: BigInt, decimals: Int) : this(BigNum(raw, decimals))
    constructor(raw: BigInt, token: DecimalsToken) : this(raw, token.decimals)
    constructor(amount: BigNum, decimals: Int) : this(amount.convertToScale(otherScale = decimals))
    constructor(amount: BigNum, token: DecimalsToken) : this(amount, token.decimals)

    val raw: BigInt get() = amount.int

    operator fun plus(other: TokenAmount) = TokenAmount(amount = amount + other.amount)
    operator fun minus(other: TokenAmount) = TokenAmount(amount = amount - other.amount)
    operator fun div(other: TokenAmount) = TokenAmount(amount = amount / other.amount)
    operator fun times(other: TokenAmount) = TokenAmount(amount = amount * other.amount)
}

fun DecimalsToken.amountRaw(raw: BigInt) = TokenAmount(raw, token = this)
fun DecimalsToken.amount(amount: BigNum) = TokenAmount(amount, token = this)
