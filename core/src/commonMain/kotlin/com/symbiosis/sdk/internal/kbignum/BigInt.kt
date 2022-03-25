package com.symbiosis.sdk.internal.kbignum

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi

internal val BigInt.Companion.UINT256_MAX: BigInt
    get() = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff".bi(radix = 16)
