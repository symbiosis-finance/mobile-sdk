package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.configuration.SwapTTLProvider
import com.symbiosis.sdk.network.Network
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.entity.ContractAddress
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.entity.TransactionHash
import dev.icerock.moko.web3.signing.Credentials

class RouterContract internal constructor(
    private val executor: Web3Executor,
    private val network: Network,
    val wrapped: SmartContract,
    private val defaultSwapTTLProvider: SwapTTLProvider,
    private val tokenContractFactory: (ContractAddress) -> TokenContract
) {
    private suspend fun defaultDeadline() = provideDeadline(defaultSwapTTLProvider.getSwapTTL(network.chainId))

    suspend fun swapExactTokensForTokens(
        credentials: Credentials,
        amountIn: BigInt,
        amountOutMin: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        recipient: EthereumAddress = credentials.address
    ): TransactionHash {
        tokenContractFactory(path.first()).approveMaxIfNeed(
            credentials = credentials,
            spender = wrapped.contractAddress,
            amount = amountIn
        )
        return wrapped.write(
            credentials = credentials,
            method = "swapExactTokensForTokens",
            params = listOf(
                amountIn,
                amountOutMin,
                path,
                recipient,
                deadline ?: defaultDeadline()
            )
        )
    }

    suspend fun getSwapExactTokensForTokensCallData(
        amountIn: BigInt,
        amountOutMin: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        recipient: EthereumAddress
    ) = wrapped.writeRequest(
        method = "swapExactTokensForTokens",
        params = listOf(
            amountIn, amountOutMin,
            path,
            recipient,
            deadline ?: defaultDeadline()
        )
    ).callData

    suspend fun swapTokensForExactTokens(
        credentials: Credentials,
        amountInMax: BigInt,
        amountOut: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        recipient: EthereumAddress = credentials.address
    ): TransactionHash {
        tokenContractFactory(path.first()).approveMaxIfNeed(
            credentials = credentials,
            spender = wrapped.contractAddress,
            amount = amountInMax
        )

        return wrapped.write(
            credentials = credentials,
            method = "swapTokensForExactTokens",
            params = listOf(
                amountOut,
                amountInMax,
                path,
                recipient,
                deadline ?: defaultDeadline()
            )
        )
    }

    suspend fun swapExactNativeForTokens(
        credentials: Credentials,
        amountInNative: BigInt,
        amountOutMin: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        recipient: EthereumAddress = credentials.address
    ) = wrapped.write(
        credentials = credentials,
        method = "swapExactETHForTokens",
        params = listOf(
            amountOutMin,
            path,
            recipient,
            deadline ?: defaultDeadline()
        ),
        value = amountInNative
    )

    suspend fun getSwapExactNativeForTokensCallData(
        amountOutMin: BigInt,
        path: List<ContractAddress>,
        recipient: EthereumAddress,
        deadline: BigInt? = null,
    ) = wrapped.writeRequest(
        method = "swapExactETHForTokens",
        params = listOf(
            amountOutMin,
            path,
            recipient,
            deadline ?: defaultDeadline()
        )
    ).callData

    suspend fun swapNativeForExactTokens(
        credentials: Credentials,
        amountInMaxNative: BigInt,
        amountOut: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        recipient: EthereumAddress = credentials.address
    ) = wrapped.write(
        credentials = credentials,
        method = "swapETHForExactTokens",
        params = listOf(
            amountOut,
            path,
            recipient,
            deadline ?: defaultDeadline()
        ),
        value = amountInMaxNative
    )

    suspend fun swapTokensForExactNative(
        credentials: Credentials,
        amountInMax: BigInt,
        amountOutNative: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        recipient: EthereumAddress = credentials.address
    ): TransactionHash {
        tokenContractFactory(path.first()).approveMaxIfNeed(
            credentials = credentials,
            spender = wrapped.contractAddress,
            amount = amountInMax,
        )

        return wrapped.write(
            credentials = credentials,
            method = "swapTokensForExactETH",
            params = listOf(
                amountOutNative,
                amountInMax,
                path,
                recipient,
                deadline ?: defaultDeadline()
            )
        )
    }

    suspend fun swapExactTokensForNative(
        credentials: Credentials,
        amountIn: BigInt,
        amountOutMin: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        recipient: EthereumAddress = credentials.address
    ): TransactionHash {
        tokenContractFactory(path.first()).approveMaxIfNeed(
            credentials = credentials,
            spender = wrapped.contractAddress,
            amount = amountIn
        )

        return wrapped.write(
            credentials = credentials,
            method = "swapExactTokensForETH",
            params = listOf(
                amountIn,
                amountOutMin,
                path,
                recipient,
                deadline ?: defaultDeadline()
            )
        )
    }

    suspend fun getSwapExactTokensForNativeCallData(
        amountIn: BigInt,
        amountOutMin: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        recipient: EthereumAddress
    ) = wrapped.writeRequest(
        method = "swapExactTokensForETH",
        params = listOf(
            amountIn, amountOutMin,
            path,
            recipient, deadline ?: defaultDeadline()
        )
    ).callData
}

data class SwapTopic(
    val amount0In: BigInt,
    val amount1In: BigInt,
    val amount0Out: BigInt,
    val amount1Out: BigInt
)

fun LogEvent.parseSwapTopic(): SwapTopic =
    deserializeData { (amount0In, amount1In, amount0Out, amount1Out) ->
        SwapTopic(amount0In.bigInt, amount1In.bigInt, amount0Out.bigInt, amount1Out.bigInt)
    }
