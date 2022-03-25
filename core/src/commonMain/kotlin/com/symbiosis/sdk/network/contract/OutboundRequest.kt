package com.symbiosis.sdk.network.contract

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.ContractAddress
import dev.icerock.moko.web3.EthereumAddress
import dev.icerock.moko.web3.WalletAddress

sealed interface OutboundRequest {
    val recipient: EthereumAddress
    val chain2address: WalletAddress
    val amount: BigInt
    val state: State
    val tokenAddress: ContractAddress


    enum class State {
        Default, Sent, Reverted
    }
}
