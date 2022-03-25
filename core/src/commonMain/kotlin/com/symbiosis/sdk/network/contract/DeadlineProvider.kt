package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.bi
import com.symbiosis.sdk.internal.time.timeMillis

internal fun provideDeadline(offset: BigInt): BigInt = timeMillis.bi + offset
