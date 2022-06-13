package com.symbiosis.sdk.network.contract.abi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray

//language=json
private const val ABI = """
[
    {
        "inputs": [],
        "stateMutability": "nonpayable",
        "type": "constructor"
    },
    {
        "inputs": [
            {
                "internalType": "address",
                "name": "_token",
                "type": "address"
            },
            {
                "internalType": "uint256",
                "name": "_amount",
                "type": "uint256"
            },
            {
                "internalType": "address",
                "name": "_receiveSide",
                "type": "address"
            },
            {
                "internalType": "bytes",
                "name": "_calldata",
                "type": "bytes"
            },
            {
                "internalType": "uint256",
                "name": "_offset",
                "type": "uint256"
            }
        ],
        "name": "externalCall",
        "outputs": [],
        "stateMutability": "nonpayable",
        "type": "function"
    },
    {
        "inputs": [
            {
                "components": [
                    {
                        "internalType": "uint256",
                        "name": "stableBridgingFee",
                        "type": "uint256"
                    },
                    {
                        "internalType": "uint256",
                        "name": "amount",
                        "type": "uint256"
                    },
                    {
                        "internalType": "bytes32",
                        "name": "externalID",
                        "type": "bytes32"
                    },
                    {
                        "internalType": "address",
                        "name": "tokenReal",
                        "type": "address"
                    },
                    {
                        "internalType": "uint256",
                        "name": "chainID",
                        "type": "uint256"
                    },
                    {
                        "internalType": "address",
                        "name": "to",
                        "type": "address"
                    },
                    {
                        "internalType": "address[]",
                        "name": "swapTokens",
                        "type": "address[]"
                    },
                    {
                        "internalType": "address",
                        "name": "secondDexRouter",
                        "type": "address"
                    },
                    {
                        "internalType": "bytes",
                        "name": "secondSwapCalldata",
                        "type": "bytes"
                    },
                    {
                        "internalType": "address",
                        "name": "finalReceiveSide",
                        "type": "address"
                    },
                    {
                        "internalType": "bytes",
                        "name": "finalCalldata",
                        "type": "bytes"
                    },
                    {
                        "internalType": "uint256",
                        "name": "finalOffset",
                        "type": "uint256"
                    }
                ],
                "internalType": "struct MetaRouteStructs.MetaMintTransaction",
                "name": "_metaMintTransaction",
                "type": "tuple"
            }
        ],
        "name": "metaMintSwap",
        "outputs": [],
        "stateMutability": "nonpayable",
        "type": "function"
    },
    {
        "inputs": [
            {
                "components": [
                    {
                        "internalType": "bytes",
                        "name": "firstSwapCalldata",
                        "type": "bytes"
                    },
                    {
                        "internalType": "bytes",
                        "name": "secondSwapCalldata",
                        "type": "bytes"
                    },
                    {
                        "internalType": "address[]",
                        "name": "approvedTokens",
                        "type": "address[]"
                    },
                    {
                        "internalType": "address",
                        "name": "firstDexRouter",
                        "type": "address"
                    },
                    {
                        "internalType": "address",
                        "name": "secondDexRouter",
                        "type": "address"
                    },
                    {
                        "internalType": "uint256",
                        "name": "amount",
                        "type": "uint256"
                    },
                    {
                        "internalType": "bool",
                        "name": "nativeIn",
                        "type": "bool"
                    },
                    {
                        "internalType": "address",
                        "name": "relayRecipient",
                        "type": "address"
                    },
                    {
                        "internalType": "bytes",
                        "name": "otherSideCalldata",
                        "type": "bytes"
                    }
                ],
                "internalType": "struct MetaRouteStructs.MetaRouteTransaction",
                "name": "_metarouteTransaction",
                "type": "tuple"
            }
        ],
        "name": "metaRoute",
        "outputs": [],
        "stateMutability": "payable",
        "type": "function"
    },
    {
        "inputs": [],
        "name": "metaRouterGateway",
        "outputs": [
            {
                "internalType": "contract MetaRouterGateway",
                "name": "",
                "type": "address"
            }
        ],
        "stateMutability": "view",
        "type": "function"
    }
]
"""

internal val metaRouterV2Contract: JsonArray = Json.parseToJsonElement(ABI).jsonArray
