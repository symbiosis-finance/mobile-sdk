# Wallets

For working with any transactions, we should start from wallets.
Every write transaction requires `Credentials` as a parameter,
so you can get it only from KeyPhrase (at least for now).

Wallet for blockchain means a private key.
**If the private key matches the format then it is correct for transactions.**
So, there is no remote validation.

## Wallet authorization:
```kotlin
val keyPhrase = KeyPhrase.wrapChecked(
    keyPhrase = "remind enter project steak copper sail depart rubber armor brisk color know"
) ?: error("This keyphrase is invalid")
```
> At this step we are wrapping our string to KeyPhrase.
The value class checks if the string is valid and
in case of invalidity returns null, this is the
recommended way to create KeyPhrase.

```kotlin
val credentials = Credentials.createFromKeyPhrase(keyPhrase)
```
> Credentials class stores public address (in this case resolved 
> automatically from private key) and signer that can sign the transaction

### Credentials

Credentials is all you need to send transactions. You can also define your own signer to use 
with MetaMask or other service like this:
```kotlin
class MetaMaskSigner(
    private val address: WalletAddress
) : TransactionSigner.Async {
    override suspend fun signTransferTransaction(
        nonce: BigInt,
        chainId: BigInt,
        to: WalletAddress,
        value: BigInt,
        gasConfiguration: GasConfiguration
    ): SignedTransaction = TODO()

    override suspend fun signContractTransaction(
        nonce: BigInt,
        chainId: BigInt,
        to: ContractAddress,
        contractData: String,
        value: BigInt,
        gasConfiguration: GasConfiguration
    ): SignedTransaction = TODO()
}

fun Credentials.Companion.createFromMetaMask(address: WalletAddress) = 
    Credentials(address, MetaMaskSigner(address))

val metaMask = Credentials.createFromMetaMask(/*...*/)
```
