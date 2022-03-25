# Swaps
Here I will explain all details of the Swap Algorithm.
First, you should note that there are 2 types of swaps: 
- **Exact In**: You know how much you want to spend, but don't know 
how much you will get
- **Exact Out**: You know how much you want to get, but don't know how
much you will spend

## One chain
In fact, every swap can be performed within one network, but here will be
some info about how to pass this restriction. Generally swap can be separated to 2 steps.

### Search for the best trade
Since we need to save the blockchain resources (or pay a big commission),
this step should be done with the client. This work is delegated on the library,
so you don't need to know details for now, but you need to know how to do this with
sdk: 
```kotlin
val trade: CalculatedSwapTrade = sdk.ethRinkeby.swap.findBestTrade(
    networkTokenPair = NetworkTokenPair(
        first = token1,
        second = token2
    ),  // Separate class is used to explicitly mark hat these tokens is from one network
    value = 1.bi, // Depends on what type you have chosen: value input or value output.
    type = SwapType // SwapType.ExactIn or SwapType.ExactOut
)
```
_If you do the swap with a native currency, you should replace it with [wrapped representation](https://academy.binance.com/en/articles/what-are-wrapped-tokens)_

**Resulting object should be checked:**
- if the trade is null, then no valid paths were found
- if the trade is CalculatedSwapTrade.ExactOut.InsufficientLiquidity then
value was more than was in reserves

Note that for ExactIn trade type you should check only for nullability. This type
of trade is always success, so there are shortcut:

```kotlin
val trade: CalculatedSwapTrade.ExactIn? = sdk.ethRinkeby.swap.findBestTradeExactIn(
    networkTokenPair = NetworkTokenPair(
        first = token1,
        second = token2
    ),  // Separate class is used to explicitly mark hat these tokens is from one network
    amountIn = 10_000_000_000_000_000.bi
)
```

There is a same function for ExactOut trade:
```kotlin
val trade: CalculatedSwapTrade.ExactOut? = sdk.ethRinkeby.swap.findBestTradeExactOut(
    networkTokenPair = NetworkTokenPair(
        first = token1,
        second = token2
    ),  // Separate class is used to explicitly mark hat these tokens is from one network
    amountOut = 10_000_000_000_000_000.bi
)
```

### Evaluate the swap
There are two cases when evaluating the swap that I will review now

#### Swap without native currencies
This is the easiest one because it only requires to call this method:
```kotlin
val hash: TransactionHash = sdk.ethRinkeby.swap.execute(
    credentials = yourCredentials,
    trade = trade, // trade you got from previous step
    // Optional
    slippage = slippage, // allowed deviation from amountIn/amountOut
    deadline = time // time when swap becomes invalid
)
```

#### Swap with native currencies
This one is a bit tricky, but is not less useful nevertheless.
If one of currencies you are swapping is a native currency, you
should use `executeWithNative` method, but first you need to resolve
`isNativeExact` variable. 

The rule is simple: 
- If your trade type is ExactIn and your native token is In: pass `true`
- If your trade type is ExactOut and your native token is Out: pass `true` 
- otherwise pass `false`

In other words, if you want to get/spend **exact** amount of native token - pass `true`.

And the code:
```kotlin
val hash: TransactionHash = sdk.ethRinkeby.swap.executeWithNative(
    credentials = yourCredentials,
    trade = trade, // trade you got from previous step
    isNativeExact = isNativeExact, // explanation above
    // Optional
    slippage = slippage, // allowed deviation from amountIn/amountOut
    deadline = time // time when swap becomes invalid
)
```

## Cross chain
For cross chain swaps we should do a little trick to make them one chain. 
This trick is called synthetic representation (more you can read [here](synthetic-tokens.md)).

**Main rule:** regardless of swap type, swap should be performed in the 
network of first token.

With this rule for every swap type in cross chain swap we pay with real tokens and receive 
synth tokens, so after swap we should send the un synth transaction with
amount we've received.
