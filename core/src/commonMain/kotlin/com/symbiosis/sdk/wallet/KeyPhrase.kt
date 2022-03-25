package com.symbiosis.sdk.wallet

import kotlin.jvm.JvmInline

/**
 * Инстанс этого класса всегда должен содержать в себе валидную мнемоник фразу
 */
@JvmInline
expect value class KeyPhrase private constructor(val value: String) {
    companion object {
        fun generate(): KeyPhrase
        fun wrapChecked(keyPhrase: String): KeyPhrase?
    }
}
