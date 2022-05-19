package com.symbiosis.sdk.swap

/**
 * This is the class to distinguish between different cases of native tokens' usage
 */
// TODO: Get rid of this class
object TokensType {
    // all objects
    sealed interface CrossChainTokensType
    // all objects except BothErc20Tokens
    sealed interface HasNativeTokens : CrossChainTokensType
    // all objects except BothNativeTokens
    sealed interface OnChainTokensType : CrossChainTokensType

    object BothNative : CrossChainTokensType, HasNativeTokens
    object BothErc20 : CrossChainTokensType, OnChainTokensType
    object FirstNative : CrossChainTokensType, OnChainTokensType, HasNativeTokens
    object LastNative : CrossChainTokensType, OnChainTokensType, HasNativeTokens
}
