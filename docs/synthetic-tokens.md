# Synthetic Tokens

Swaps may be only performed within 1 network, so to overcome this limitation
we should get alternative tokens in one network and then do swap.

This alternative tokens called synthetic. Such tokens can be freely exchanged 
in two ways between networks (with commission), so you cannot swap ETH to Pancake 
(BSC) directly, but you can swap ETH to Synthetic Pancake and then exchange 
sPancake to Pancake with 1 to 1 rate (but with 0.01 eth commission)

## Synthesizing Contracts
There are 2 different contracts on 2 networks. One is for getting synthetic tokens from
real, another is for getting real tokens:

### Portal
First contract called `portal` because you
are dropping real tokens there, and they are like "teleporting" to another network in
synthetic equivalent. 

Code to do that: 
```kotlin
sdk.ethRinkeby.portal.synthesize(
    credentials = yourCredentials,
    amount = 1.0.bn, // Amount of real tokens you want to spend
    realCurrencyAddress = targetToken.address, // Address of real token you want to spend
    targetNetwork = Networks.BscTestnet
)
```

### Router
Second contract called `synthesize`, with the contract, you can like "burn" your 
created synthetic tokens and receive real tokens for them.

Code to do that:
```kotlin
sdk.ethRinkeby.synthesize.burnSynthTokens(
    credentials = yourCredentials,
    amount = 1.0.bn, // Amount of synthetic tokens you want to spend
    synthCurrencyAddress = synthCurrencyAddress, // Address of synthetic tokens you want to spend
    targetNetwork = Networks.Rinkeby
)
```

## Synthetic Fabric Contract
But how to get the synthetic currency address from the last example? 
For this we have a separate contract called Synthetic Fabric Contract. 
It has 2 main methods.

### getSyntheticToken
This method should be called on the NetworkClient of synthesize target network.

**Example:** We want to get synthetic pair for UNI (Rinkeby) on BscTestnet

**Code:** 
```kotlin 
sdk.ethRinkeby.synthFabric.getSyntheticToken(
    address = TokenCryptoCurrencies.RinkebyUNI.address, // address of original token
    chainId = TokenCryptoCurrencies.RinkebyUNI.chainId // chain id where the original token takes place
)
```

There are also an extension method for convenience:
```kotlin
sdk.ethRinkeby.synthFabric.getSyntheticToken(
    currency = TokenCryptoCurrencies.RinkebyUNI
)
```

### getRealTokenAddress
This method should also be called on the NetworkClient where synthetic token is.
Sadly, this method cannot return TokenCryptoCurrency because the target network is 
unknown.

**Example:**: We want to get real token address for sUNI (BscTestnet)

**Code**:
```kotlin
sdk.bscTestnet.synthFabric.getRealTokenAddress(
    synthAddress = sUNI.address, // sUNI is the token we can get with the previous article
)
```

For convenience there is also an extension method:
```kotlin
sdk.bscTestnet.synthFabric.getRealTokenAddress(
    currency = sUNI, // sUNI is the token we can get with the previous article
)
```

## Pay attention
All methods of Synthetic Fabric should be called within the network where synthetic
tokens take place:
- Want to get synth token of ETH currency on BSC? Call it on BSC, not on ETH
- Want to get real ETH token from synth token on BSC? Call it on BSC, not on ETH

It may cause many errors with, so be careful.
