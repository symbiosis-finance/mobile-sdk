package com.symbiosis.sdk.network.contract

import dev.icerock.moko.web3.crypto.KeccakParameter
import dev.icerock.moko.web3.crypto.digestKeccak
import dev.icerock.moko.web3.entity.LogEvent
import dev.icerock.moko.web3.hex.Hex32String

fun List<LogEvent>.filterEventsWithTopics(vararg topics: Hex32String) = filterEventsWithTopics(topics.toList())

fun List<LogEvent>.filterEventsWithTopics(topics: List<Hex32String>) = filter { event ->
    if (topics.size > event.topics.size)
        return@filter false

    return@filter topics.zip(event.topics).all { (conditionTopic, actualTopic) -> conditionTopic == actualTopic }
}

fun List<LogEvent>.requireBurnRequestEvent() = filterEventsWithTopics(
    Hex32String(
        "BurnRequest(bytes32,address,uint256,address,address,uint256,address)"
            .digestKeccak(KeccakParameter.KECCAK_256)
    )
).first()

fun List<LogEvent>.requireSynthesizeRequestEvent() = filterEventsWithTopics(
    Hex32String(
        "SynthesizeRequest(bytes32,address,uint256,address,address,uint256,address)"
            .digestKeccak(KeccakParameter.KECCAK_256)
    )
).first()
