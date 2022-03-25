package com.symbiosis.sdk.contract

import dev.icerock.moko.web3.EthereumAddress

fun List<EthereumAddress>.sortedAddresses() = sortedBy(EthereumAddress::withoutPrefix)
