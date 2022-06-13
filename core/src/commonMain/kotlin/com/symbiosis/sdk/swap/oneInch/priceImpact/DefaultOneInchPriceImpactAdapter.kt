package com.symbiosis.sdk.swap.oneInch.priceImpact

import com.soywiz.kbignum.BigInt
import com.soywiz.kbignum.BigNum
import com.soywiz.kbignum.bi
import com.soywiz.kbignum.bn
import com.symbiosis.sdk.network.NetworkClient
import com.symbiosis.sdk.network.contract.OracleContract
import com.symbiosis.sdk.swap.oneInch.OneInchToken
import com.symbiosis.sdk.swap.oneInch.OneInchTokenPair

class DefaultOneInchPriceImpactAdapter(
    private val networkClient: NetworkClient,
    private val oracleContract: OracleContract
) : OneInchPriceImpactRepository.Adapter {
    override fun getRateRequests(tokens: OneInchTokenPair): OneInchPriceImpactRepository.RateRequests {
        val requests = listOf(
            oracleContract.getRateToEthRequest(
                srcToken = tokens.first,
                useWrappers = true
            ),
            oracleContract.getRateToEthRequest(
                srcToken = tokens.second,
                useWrappers = true
            ),
        )

        return object : OneInchPriceImpactRepository.RateRequests {
            override suspend fun fetch(): OneInchPriceImpactRepository.TokenAmounts {
                val (inputRate, outputRate) = networkClient.executeBatch(requests)

                return OneInchPriceImpactRepository.TokenAmounts(
                    inputTokenRateToEth = nominate(inputRate, tokens.first),
                    outputTokenRateToEth = nominate(outputRate, tokens.second)
                )
            }
        }
    }

    // https://github.com/1inch/spot-price-aggregator/blob/351904ad01d8163982e520fbb7c753b05997b7c1/examples/single-price.js#L22
    private fun nominate(raw: BigInt, token: OneInchToken): BigNum {
        if (raw == 0.bi)
            return 1.bn

        val numerator = 10.bn.pow(token.decimals)
        val denominator = 10.bn.pow(18) // native decimals

        return raw
            .toBigNum()
            .times(numerator)
            .div(denominator, precision = 18)
            .div(denominator, precision = 18)
    }
}
