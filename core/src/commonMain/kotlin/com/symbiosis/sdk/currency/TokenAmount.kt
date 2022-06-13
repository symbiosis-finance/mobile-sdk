package com.symbiosis.sdk.currency

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum

class TokenAmount(amount: BigNum, val token: DecimalsToken) {
    val amount = amount.convertToScale(token.decimals)

    constructor(raw: BigInt, token: DecimalsToken) : this(BigNum(raw, token.decimals), token)

    val raw: BigInt get() = amount.int

    operator fun plus(other: TokenAmount): TokenAmount {
        require(this.token == other.token) { "Cannot sum up different tokens" }
        return TokenAmount(amount = amount + other.amount, token)
    }
    operator fun minus(other: TokenAmount): TokenAmount {
        require(this.token == other.token) { "Cannot minus different tokens" }
        return TokenAmount(amount = amount - other.amount, token)
    }
    operator fun div(other: TokenAmount): TokenAmount {
        require(this.token == other.token) { "Cannot div different tokens" }
        return TokenAmount(amount = amount / other.amount, token)
    }
    operator fun times(other: TokenAmount): TokenAmount {
        require(this.token == other.token) { "Cannot multiply different tokens" }
        return TokenAmount(amount = amount * other.amount, token)
    }

    override fun toString(): String {
        return "TokenAmount(token=$token, amount=$amount)"
    }
}

fun DecimalsToken.amountRaw(raw: BigInt) = TokenAmount(raw, token = this)
fun DecimalsToken.amount(amount: BigNum) = TokenAmount(amount, token = this)
