package com.symbiosis.sdk.wallet

import com.symbiosis.sdk.transaction.TransactionSigner
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.hex.Hex32String

expect class Credentials(address: WalletAddress, signer: TransactionSigner) {
    val address: WalletAddress
    val signer: TransactionSigner

    companion object {
        fun createFromKeyPhrase(keyPhrase: KeyPhrase): Credentials
        fun createFromPrivateKey(key: Hex32String): Credentials
    }
}

@Throws(Throwable::class)
fun Credentials.Companion.createFromKeyPhrase(keyPhrase: String): Credentials? {
    val phrase = KeyPhrase.wrapChecked(keyPhrase) ?: return null
    return createFromKeyPhrase(phrase)
}

@Throws(Throwable::class)
fun Credentials.Companion.createFromPrivateKey(privateKey: String): Credentials? {
    val key = try { Hex32String(privateKey) } catch (_: IllegalArgumentException) { return null }
    return createFromPrivateKey(key)
}

@Throws(Throwable::class)
fun Credentials.Companion.createFromKeyPhraseOrPrivateKey(value: String): Credentials? =
    when (value.split(" ").size) {
        1 -> createFromPrivateKey(value)
        else -> createFromKeyPhrase(value)
    }
