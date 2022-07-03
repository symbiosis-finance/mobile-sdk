package com.symbiosis.sdk.swap

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.internal.kbignum.toBigNum

class Percentage(val fractionalValue: BigNum) {
    val intPercentage: BigInt get() = fractionalValue.convertToScale(otherScale = 2).int
    val fractionalPercentage: BigNum get() = (fractionalValue * 100.bn)

    operator fun plus(other: Percentage) =
        Percentage(fractionalValue = fractionalValue + other.fractionalValue)
    operator fun minus(other: Percentage) =
        Percentage(fractionalValue = fractionalValue - other.fractionalValue)
    operator fun times(other: Percentage) =
        Percentage(fractionalValue = fractionalValue * other.fractionalPercentage)
    operator fun div(other: Percentage) =
        Percentage(fractionalValue = fractionalValue / other.fractionalValue)

    operator fun compareTo(other: Percentage): Int = when {
        fractionalValue < other.fractionalValue -> -1
        fractionalValue == other.fractionalValue -> 0
        fractionalValue > other.fractionalValue -> 1
        else -> error("Unreachable")
    }

    operator fun compareTo(fractionalValue: BigNum): Int = compareTo(Percentage(fractionalValue))

    operator fun compareTo(intPercentage: BigInt): Int = compareTo(
        Percentage(fractionalValue = intPercentage.toBigNum().div(100.bn, precision = 2))
    )

    operator fun compareTo(intPercentage: Int): Int = compareTo(intPercentage.bi)

    override fun toString(): String = "$fractionalPercentage%"
}
