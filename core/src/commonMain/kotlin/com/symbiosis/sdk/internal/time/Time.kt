package com.symbiosis.sdk.internal.time

internal expect val timeMillis: Long

internal val Int.secondsAsMillis get() = this * 1_000
val Int.minutesAsMillis get() = (this * 60).secondsAsMillis
