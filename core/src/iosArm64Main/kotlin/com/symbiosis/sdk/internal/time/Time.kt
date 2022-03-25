package com.symbiosis.sdk.internal.time

import platform.Foundation.NSDate

actual val timeMillis: Long get() = (NSDate().timeIntervalSinceReferenceDate() * 1000).toLong()
