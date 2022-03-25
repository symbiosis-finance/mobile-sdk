# Tokens

For the library there are 2 types of cryptocurrencies: 
native cryptocurrency and token cryptocurrency

The native one is that currency that associated with network.
For Ethereum, it is ETH, for Binance Smart Chain it is BNB, etc.

`CryptoCurrency` is the base for all currencies. 
For native currencies `NativeCryptoCurrency` interface is used,
while for token currencies there is `TokenCryptoCurrency`

There are also an util class for converting decimal amount to integer amount of cryptocurrency
- `CryptoCurrencyAmount(amount, decimals)`. 
- more convenient way to use with cryptocurrency: `CryptoCurrencyAmount(amount, currency)`.
- and, finally, the extension: `currency.amount(2.1.bn)`
