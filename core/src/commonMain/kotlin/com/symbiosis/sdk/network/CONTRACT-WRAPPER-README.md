# Contracts and Wrappers

Simple explanation how the contracts was implemented.
The final result we wanted to achieve is type-safe api
for the contracts like:
```kotlin
sdk.ethRinkeby.portal.synthesize(/* params with hints from ide */)
```

## Layers
So there are 3 layers for that purpose was created

### Low-level ABI layer
[This layer](contract/abi) contains only a raw ABI of our contract

### Medium-level Contract layer
At [this layer](contract) we are wrapping the ABIs with type safe api, like
[here](contract/TokenContract.kt). 
For a bunch of contracts it is a final layer.

### High-level Wrapper layer
But some contracts have a lot of different functions we should
call when different params passed. The idea of the second layer
was **only** to wrap raw contract abi, so it should not contain
any business logic.

For cases when we even so need a business logic, [this layer](wrapper)
was introduced, and it wraps Contract layer just like it wraps ABI layer.

See: execute/executeWithNative method in [example](wrapper/SwapWrapper.kt).
