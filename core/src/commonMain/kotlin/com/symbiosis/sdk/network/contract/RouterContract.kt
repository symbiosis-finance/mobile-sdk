package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import com.symbiosis.sdk.configuration.GasProvider
import com.symbiosis.sdk.configuration.SwapTTLProvider
import com.symbiosis.sdk.contract.write
import com.symbiosis.sdk.internal.nonce.NonceController
import com.symbiosis.sdk.network.Network
import com.symbiosis.sdk.wallet.Credentials
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.TransactionHash
import dev.icerock.moko.web3.Web3Executor
import dev.icerock.moko.web3.contract.SmartContract
import dev.icerock.moko.web3.entity.LogEvent

class RouterContract internal constructor(
    private val executor: Web3Executor,
    private val network: Network,
    private val nonceController: NonceController,
    private val wrapped: SmartContract,
    private val defaultGasProvider: GasProvider,
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
        gasProvider: GasProvider? = null,
        recipient: BigInt = credentials.address.bigInt
    ): TransactionHash {
        tokenContractFactory(path.first()).approveMaxIfNeed(
            credentials = credentials,
            spender = wrapped.contractAddress,
            amount = amountIn,
            gasProvider = gasProvider
        )
        return wrapped.write(
            chainId = network.chainId,
            nonceController = nonceController,
            credentials = credentials,
            method = "swapExactTokensForTokens",
            params = listOf(
                amountIn,
                amountOutMin,
                path.map(ContractAddress::bigInt),
                recipient,
                deadline ?: defaultDeadline()
            ),
            gasProvider = gasProvider ?: defaultGasProvider
        )
    }

    suspend fun getSwapExactTokensForTokensCallData(
        amountIn: BigInt,
        amountOutMin: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        recipient: EthereumAddress
    ) = wrapped.encodeMethod(
        method = "swapExactTokensForTokens",
        params = listOf(
            amountIn, amountOutMin,
            path.map(ContractAddress::bigInt),
            recipient.bigInt,
            deadline ?: defaultDeadline()
        )
    )

    suspend fun swapTokensForExactTokens(
        credentials: Credentials,
        amountInMax: BigInt,
        amountOut: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        gasProvider: GasProvider? = null,
        recipient: BigInt = credentials.address.bigInt
    ): TransactionHash {
        tokenContractFactory(path.first()).approveMaxIfNeed(
            credentials = credentials,
            spender = wrapped.contractAddress,
            amount = amountInMax,
            gasProvider = gasProvider
        )

        return wrapped.write(
            chainId = network.chainId,
            nonceController = nonceController,
            credentials = credentials,
            method = "swapTokensForExactTokens",
            params = listOf(
                amountOut,
                amountInMax,
                path.map(ContractAddress::bigInt),
                recipient,
                deadline ?: defaultDeadline()
            ),
            gasProvider = gasProvider ?: defaultGasProvider
        )
    }

    suspend fun swapExactNativeForTokens(
        credentials: Credentials,
        amountInNative: BigInt,
        amountOutMin: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        gasProvider: GasProvider? = null,
        recipient: BigInt = credentials.address.bigInt
    ) = wrapped.write(
        chainId = network.chainId,
        nonceController = nonceController,
        credentials = credentials,
        method = "swapExactETHForTokens",
        params = listOf(
            amountOutMin,
            path.map(ContractAddress::bigInt),
            recipient,
            deadline ?: defaultDeadline()
        ),
        gasProvider = gasProvider ?: defaultGasProvider,
        value = amountInNative
    )

    suspend fun getSwapExactNativeForTokensCallData(
        amountOutMin: BigInt,
        path: List<ContractAddress>,
        recipient: EthereumAddress,
        deadline: BigInt? = null,
    ) = wrapped.encodeMethod(
        method = "swapExactETHForTokens",
        params = listOf(
            amountOutMin,
            path.map(ContractAddress::bigInt),
            recipient.bigInt,
            deadline ?: defaultDeadline()
        )
    )

    suspend fun swapNativeForExactTokens(
        credentials: Credentials,
        amountInMaxNative: BigInt,
        amountOut: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        gasProvider: GasProvider? = null,
        recipient: BigInt = credentials.address.bigInt
    ) = wrapped.write(
        chainId = network.chainId,
        nonceController = nonceController,
        credentials = credentials,
        method = "swapETHForExactTokens",
        params = listOf(
            amountOut,
            path.map(ContractAddress::bigInt),
            recipient,
            deadline ?: defaultDeadline()
        ),
        gasProvider = gasProvider ?: defaultGasProvider,
        value = amountInMaxNative
    )

    suspend fun swapTokensForExactNative(
        credentials: Credentials,
        amountInMax: BigInt,
        amountOutNative: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        gasProvider: GasProvider? = null,
        recipient: BigInt = credentials.address.bigInt
    ): TransactionHash {
        tokenContractFactory(path.first()).approveMaxIfNeed(
            credentials = credentials,
            spender = wrapped.contractAddress,
            amount = amountInMax,
            gasProvider = gasProvider
        )

        return wrapped.write(
            chainId = network.chainId,
            nonceController = nonceController,
            credentials = credentials,
            method = "swapTokensForExactETH",
            params = listOf(
                amountOutNative,
                amountInMax,
                path.map(ContractAddress::bigInt),
                recipient,
                deadline ?: defaultDeadline()
            ),
            gasProvider = gasProvider ?: defaultGasProvider
        )
    }

    suspend fun swapExactTokensForNative(
        credentials: Credentials,
        amountIn: BigInt,
        amountOutMin: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        gasProvider: GasProvider? = null,
        recipient: BigInt = credentials.address.bigInt
    ): TransactionHash {
        tokenContractFactory(path.first()).approveMaxIfNeed(
            credentials = credentials,
            spender = wrapped.contractAddress,
            amount = amountIn,
            gasProvider = gasProvider
        )

        return wrapped.write(
            chainId = network.chainId,
            nonceController = nonceController,
            credentials = credentials,
            method = "swapExactTokensForETH",
            params = listOf(
                amountIn,
                amountOutMin,
                path.map(ContractAddress::bigInt),
                recipient,
                deadline ?: defaultDeadline()
            ),
            gasProvider = gasProvider ?: defaultGasProvider
        )
    }

    suspend fun getSwapExactTokensForNativeCallData(
        amountIn: BigInt,
        amountOutMin: BigInt,
        path: List<ContractAddress>,
        deadline: BigInt? = null,
        recipient: EthereumAddress
    ) = wrapped.encodeMethod(
        method = "swapExactTokensForETH",
        params = listOf(
            amountIn, amountOutMin,
            path.map(ContractAddress::bigInt),
            recipient.bigInt, deadline ?: defaultDeadline()
        )
    )
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
