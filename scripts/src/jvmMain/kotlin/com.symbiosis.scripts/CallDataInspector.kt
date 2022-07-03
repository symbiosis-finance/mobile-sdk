package com.symbiosis.scripts

import com.soywiz.kbignum.BigInt
import dev.icerock.moko.web3.contract.ABIDecoder
import dev.icerock.moko.web3.contract.ABIEncoder
import dev.icerock.moko.web3.entity.EthereumAddress
import dev.icerock.moko.web3.hex.HexString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun main() {
    println("Welcome to call data inspector!")

    while (true) {
        val callData: HexString = setupCallData()

        when (callData.withoutPrefix.length % 64) {
            0 -> analyzeData(callData)
            // first four bytes is a method signature
            8 -> analyzeMethod(callData)
            else -> println("Invalid calldata provided")
        }

        println()
    }
}

private fun noAbiBranch(): JsonArray {
    println("Enter ABI for your calldata to use:")
    while (true) {
        try {
            return Json.parseToJsonElement(readLine()!!).jsonArray
        } catch (_: Throwable) {
            println("Invalid ABI provided! Try again.")
        }
    }
}

private fun setupCallData(): HexString {
    println("Enter your call data to analyze:")
    while (true) {
        try {
            return HexString(readLine()!!)
        } catch (_: Throwable) {
            println("Invalid hex string provided! Try again.")
        }
    }
}

private fun analyzeMethod(callData: HexString) {
    val abi: JsonArray = noAbiBranch()

    val methods = abi
        .filter { it.jsonObject["type"]?.jsonPrimitive?.content == "function" }
        .map { it.jsonObject }
        .associateBy {
            ABIEncoder.hashMethodSignature(
                method = it["name"]!!.jsonPrimitive.content,
                inputParams = it["inputs"]!!.jsonArray.map { param -> param.jsonObject }
            ).let(::HexString)
        }

    val signature = HexString(callData.withoutPrefix.take(n = 8))
    val method = methods[signature] ?: return println("The method with signature $signature was not found in $methods")

    val paramTypes = method["inputs"]!!.jsonArray.map { it.jsonObject }
    val decoded = ABIDecoder.decodeCallData(
        paramTypes = paramTypes,
        callData = HexString(callData.withoutPrefix.drop(n = 8)).byteArray
    )

    val decodedOutput = paramTypes
        .zip(decoded)
        .joinToString(separator = "\n") { (param, value) -> stringifyParam(param, value) }

    println(
        """
        ANALYZE RESULTS:
        Method name: ${method["name"]}
        Decoded params:
        
    """.trimIndent() + decodedOutput
    )
}

private fun stringifyParam(param: JsonObject, value: Any?): String {
    val typeName = param["type"]!!.jsonPrimitive.content
    val typeInternalName = (param["internalType"] ?: param["name"])!!.jsonPrimitive.content
    val paramName = param["name"]!!.jsonPrimitive.content

    val decodedValue: String = prettyPrintTypeValue(param, value)

    return """$typeName ($typeInternalName) "$paramName": $decodedValue"""
}

private fun prettyPrintTypeValue(param: JsonObject, value: Any?): String {
    val typeName = param["type"]!!.jsonPrimitive.content
    return when {
        typeName == "tuple" -> {
            "\n" + param["components"]!!.jsonArray
                .map { it.jsonObject }
                .zip(value as List<*>)
                .joinToString(separator = "\n") { (param, value) ->
                    stringifyParam(param, value)
                }.prependIndent()
        }
        typeName == "address" -> (value as EthereumAddress).prefixed
        typeName.startsWith("bytes") -> HexString(value as ByteArray).prefixed
        typeName.endsWith("[]") -> {
            val subparam = param
                .toMutableMap()
                .apply { put("type", JsonPrimitive(typeName.removeSuffix("[]"))) }
                .let(::JsonObject)

            (value as List<*>).map { prettyPrintTypeValue(subparam, it) }.toString()
        }
        else -> "$value"
    }
}

private fun analyzeData(callData: HexString) {
    println("This is not supported yet.")
}
