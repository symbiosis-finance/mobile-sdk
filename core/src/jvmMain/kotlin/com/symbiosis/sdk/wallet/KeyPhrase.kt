package com.symbiosis.sdk.wallet

import org.web3j.crypto.MnemonicUtils
import java.security.SecureRandom

@JvmInline
actual value class KeyPhrase private actual constructor(actual val value: String) {
    actual companion object {
        private const val ENTROPY_SIZE = 16
        // SecureRandom и так собирает энтропию с устройства
        private val secureRandom = SecureRandom()

        actual fun generate(): KeyPhrase {
            val initialEntropy = ByteArray(ENTROPY_SIZE)
            secureRandom.nextBytes(initialEntropy)

            return MnemonicUtils.generateMnemonic(initialEntropy).let(::KeyPhrase)
        }

        actual fun wrapChecked(keyPhrase: String) =
            if (MnemonicUtils.validateMnemonic(keyPhrase)) KeyPhrase(keyPhrase) else null
    }
}

val String.keyPhrase get() = KeyPhrase.wrapChecked(keyPhrase = this)
