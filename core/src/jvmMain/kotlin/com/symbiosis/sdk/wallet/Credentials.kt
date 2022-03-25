package com.symbiosis.sdk.wallet

import com.symbiosis.sdk.transaction.JavaCredentialsTransactionSigner
import com.symbiosis.sdk.transaction.TransactionSigner
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.hex.Hex32String
import org.web3j.crypto.Bip32ECKeyPair
import org.web3j.crypto.MnemonicUtils
import org.web3j.crypto.Credentials as JavaCredentials

actual class Credentials actual constructor(
    actual val address: WalletAddress,
    actual val signer: TransactionSigner
) {
    constructor(credentials: JavaCredentials) : this(
        WalletAddress(credentials.address),
        JavaCredentialsTransactionSigner(credentials)
    )

    actual companion object {
        actual fun createFromKeyPhrase(keyPhrase: KeyPhrase): Credentials {
            // https://github.com/web3j/web3j/issues/932
            // "m/44'/60'/0'/0/0"
            val path = @Suppress("MagicNumber") intArrayOf(
                44 or Bip32ECKeyPair.HARDENED_BIT,
                60 or Bip32ECKeyPair.HARDENED_BIT,
                0 or Bip32ECKeyPair.HARDENED_BIT,
                0,
                0
            )

            val seed = MnemonicUtils.generateSeed(keyPhrase.value, null)
            val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
            val bip44Keypair = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, path)

            return Credentials(JavaCredentials.create(bip44Keypair))
        }

        actual fun createFromPrivateKey(key: Hex32String): Credentials {
            return Credentials(JavaCredentials.create(key.withoutPrefix))
        }
    }
}
