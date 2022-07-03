package com.symbiosis.sdk.internal.kbignum

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bi

internal val BigInt.Companion.UINT256_MAX: BigInt
    get() = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff".bi(radix = 16)

internal fun BigInt.toBigNum() = BigNum(int = this, scale = 0)
