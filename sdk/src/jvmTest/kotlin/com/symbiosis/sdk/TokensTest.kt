package com.symbiosis.sdk

import com.soywiz.kbignum.bi
import com.symbiosis.sdk.network.networkClient
import dev.icerock.moko.web3.ContractAddress
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals

class TokensTest {
    // Test Uni Token info
    // @Test
    fun getTokenInfo() {
        runBlocking {
            val address = ContractAddress("0x1f9840a85d5af5bf1d1762f925bdaddc4201f984")
            testETH.networkClient.getTokenContract(address = address).apply {
                assertEquals(expected = "Uniswap", actual = name())
                assertEquals(expected = "UNI", actual = symbol())
                assertEquals(expected = 18.bi, actual = decimals())
            }
        }
    }
}