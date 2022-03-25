package com.symbiosis.sdk.wallet

import cocoapods.SwiftWeb3Wrapper.MnemonicUtils

actual value class KeyPhrase private actual constructor(actual val value: String) {
    actual companion object {
        actual fun generate(): KeyPhrase {
            return KeyPhrase(MnemonicUtils.generateMnemonics())
        }

        actual fun wrapChecked(keyPhrase: String): KeyPhrase? =
            if (MnemonicUtils.validateMnemonic(keyPhrase)) KeyPhrase(keyPhrase) else null
    }
}

val String.keyPhrase get() = KeyPhrase.wrapChecked(keyPhrase = this)
