package com.symbiosis.sdk.currency

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum

class TokenAmountConverter private constructor(val amount: BigNum) {
    constructor(raw: BigInt, decimals: Int) : this(BigNum(raw, decimals))
    constructor(raw: BigInt, token: DecimalsToken) : this(raw, token.decimals)
    constructor(amount: BigNum, decimals: Int) : this(amount.convertToScale(otherScale = decimals))
    constructor(amount: BigNum, token: DecimalsToken) : this(amount, token.decimals)

    val raw: BigInt = amount.int
}
