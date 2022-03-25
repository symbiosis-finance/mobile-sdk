package com.symbiosis.sdk.network.contract.abi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray

// language=json
private const val ABI = """
[
        {
            "anonymous": false,
            "inputs": [
                {
                    "indexed": false,
                    "internalType": "bytes32",
                    "name": "id",
                    "type": "bytes32"
                },
                {
                    "indexed": true,
                    "internalType": "address",
                    "name": "from",
                    "type": "address"
                },
                {
                    "indexed": true,
                    "internalType": "uint256",
                    "name": "chainID",
                    "type": "uint256"
                },
                {
                    "indexed": true,
                    "internalType": "address",
                    "name": "revertableAddress",
                    "type": "address"
                },
                {
                    "indexed": false,
                    "internalType": "address",
                    "name": "to",
                    "type": "address"
                },
                {
                    "indexed": false,
                    "internalType": "uint256",
                    "name": "amount",
                    "type": "uint256"
                },
                {
                    "indexed": false,
                    "internalType": "address",
                    "name": "token",
                    "type": "address"
                }
            ],
            "name": "BurnRequest",
            "type": "event"
        },
        {
            "anonymous": false,
            "inputs": [
                {
                    "indexed": true,
                    "internalType": "address",
                    "name": "previousOwner",
                    "type": "address"
                },
                {
                    "indexed": true,
                    "internalType": "address",
                    "name": "newOwner",
                    "type": "address"
                }
            ],
            "name": "OwnershipTransferred",
            "type": "event"
        },
        {
            "anonymous": false,
            "inputs": [
                {
                    "indexed": true,
                    "internalType": "bytes32",
                    "name": "id",
                    "type": "bytes32"
                },
                {
                    "indexed": true,
                    "internalType": "address",
                    "name": "to",
                    "type": "address"
                },
                {
                    "indexed": false,
                    "internalType": "uint256",
                    "name": "amount",
                    "type": "uint256"
                },
                {
                    "indexed": false,
                    "internalType": "uint256",
                    "name": "bridgingFee",
                    "type": "uint256"
                },
                {
                    "indexed": false,
                    "internalType": "address",
                    "name": "token",
                    "type": "address"
                }
            ],
            "name": "RevertBurnCompleted",
            "type": "event"
        },
        {
            "anonymous": false,
            "inputs": [
                {
                    "indexed": true,
                    "internalType": "bytes32",
                    "name": "id",
                    "type": "bytes32"
                },
                {
                    "indexed": true,
                    "internalType": "address",
                    "name": "to",
                    "type": "address"
                }
            ],
            "name": "RevertSynthesizeRequest",
            "type": "event"
        },
        {
            "anonymous": false,
            "inputs": [
                {
                    "indexed": true,
                    "internalType": "bytes32",
                    "name": "id",
                    "type": "bytes32"
                },
                {
                    "indexed": true,
                    "internalType": "address",
                    "name": "to",
                    "type": "address"
                },
                {
                    "indexed": false,
                    "internalType": "uint256",
                    "name": "amount",
                    "type": "uint256"
                },
                {
                    "indexed": false,
                    "internalType": "uint256",
                    "name": "bridgingFee",
                    "type": "uint256"
                },
                {
                    "indexed": false,
                    "internalType": "address",
                    "name": "token",
                    "type": "address"
                }
            ],
            "name": "SynthesizeCompleted",
            "type": "event"
        },
        {
            "inputs": [],
            "name": "bridge",
            "outputs": [
                {
                    "internalType": "address",
                    "name": "",
                    "type": "address"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "uint256",
                    "name": "_stableBridgingFee",
                    "type": "uint256"
                },
                {
                    "internalType": "address",
                    "name": "_stoken",
                    "type": "address"
                },
                {
                    "internalType": "uint256",
                    "name": "_amount",
                    "type": "uint256"
                },
                {
                    "internalType": "address",
                    "name": "_chain2address",
                    "type": "address"
                },
                {
                    "internalType": "address",
                    "name": "_receiveSide",
                    "type": "address"
                },
                {
                    "internalType": "address",
                    "name": "_oppositeBridge",
                    "type": "address"
                },
                {
                    "internalType": "address",
                    "name": "_revertableAddress",
                    "type": "address"
                },
                {
                    "internalType": "uint256",
                    "name": "_chainID",
                    "type": "uint256"
                }
            ],
            "name": "burnSyntheticToken",
            "outputs": [
                {
                    "internalType": "bytes32",
                    "name": "internalID",
                    "type": "bytes32"
                }
            ],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [],
            "name": "fabric",
            "outputs": [
                {
                    "internalType": "address",
                    "name": "",
                    "type": "address"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "address",
                    "name": "_bridge",
                    "type": "address"
                },
                {
                    "internalType": "address",
                    "name": "_trustedForwarder",
                    "type": "address"
                },
                {
                    "internalType": "contract IMetaRouterV2",
                    "name": "_metaRouter",
                    "type": "address"
                }
            ],
            "name": "initialize",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "address",
                    "name": "forwarder",
                    "type": "address"
                }
            ],
            "name": "isTrustedForwarder",
            "outputs": [
                {
                    "internalType": "bool",
                    "name": "",
                    "type": "bool"
                }
            ],
            "stateMutability": "view",
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
                            "internalType": "address",
                            "name": "syntCaller",
                            "type": "address"
                        },
                        {
                            "internalType": "address",
                            "name": "finalDexRouter",
                            "type": "address"
                        },
                        {
                            "internalType": "address",
                            "name": "sToken",
                            "type": "address"
                        },
                        {
                            "internalType": "bytes",
                            "name": "swapCallData",
                            "type": "bytes"
                        },
                        {
                            "internalType": "address",
                            "name": "chain2address",
                            "type": "address"
                        },
                        {
                            "internalType": "address",
                            "name": "receiveSide",
                            "type": "address"
                        },
                        {
                            "internalType": "address",
                            "name": "oppositeBridge",
                            "type": "address"
                        },
                        {
                            "internalType": "address",
                            "name": "revertableAddress",
                            "type": "address"
                        },
                        {
                            "internalType": "uint256",
                            "name": "chainID",
                            "type": "uint256"
                        }
                    ],
                    "internalType": "struct MetaRouteStructs.MetaBurnTransaction",
                    "name": "_metaBurnTransaction",
                    "type": "tuple"
                }
            ],
            "name": "metaBurnSyntheticToken",
            "outputs": [
                {
                    "internalType": "bytes32",
                    "name": "internalID",
                    "type": "bytes32"
                }
            ],
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
                            "name": "finalDexRouter",
                            "type": "address"
                        },
                        {
                            "internalType": "bytes",
                            "name": "finalSwapCalldata",
                            "type": "bytes"
                        }
                    ],
                    "internalType": "struct MetaRouteStructs.MetaMintTransaction",
                    "name": "_metaMintTransaction",
                    "type": "tuple"
                }
            ],
            "name": "metaMintSyntheticToken",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [],
            "name": "metaRouter",
            "outputs": [
                {
                    "internalType": "contract IMetaRouterV2",
                    "name": "",
                    "type": "address"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "uint256",
                    "name": "_stableBridgingFee",
                    "type": "uint256"
                },
                {
                    "internalType": "bytes32",
                    "name": "_externalID",
                    "type": "bytes32"
                },
                {
                    "internalType": "address",
                    "name": "_tokenReal",
                    "type": "address"
                },
                {
                    "internalType": "uint256",
                    "name": "_chainID",
                    "type": "uint256"
                },
                {
                    "internalType": "uint256",
                    "name": "_amount",
                    "type": "uint256"
                },
                {
                    "internalType": "address",
                    "name": "_to",
                    "type": "address"
                }
            ],
            "name": "mintSyntheticToken",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [],
            "name": "owner",
            "outputs": [
                {
                    "internalType": "address",
                    "name": "",
                    "type": "address"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [],
            "name": "pause",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [],
            "name": "paused",
            "outputs": [
                {
                    "internalType": "bool",
                    "name": "",
                    "type": "bool"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [],
            "name": "renounceOwnership",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [],
            "name": "requestCount",
            "outputs": [
                {
                    "internalType": "uint256",
                    "name": "",
                    "type": "uint256"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "bytes32",
                    "name": "",
                    "type": "bytes32"
                }
            ],
            "name": "requests",
            "outputs": [
                {
                    "internalType": "address",
                    "name": "recipient",
                    "type": "address"
                },
                {
                    "internalType": "address",
                    "name": "chain2address",
                    "type": "address"
                },
                {
                    "internalType": "uint256",
                    "name": "amount",
                    "type": "uint256"
                },
                {
                    "internalType": "address",
                    "name": "token",
                    "type": "address"
                },
                {
                    "internalType": "address",
                    "name": "stoken",
                    "type": "address"
                },
                {
                    "internalType": "enum Synthesis.RequestState",
                    "name": "state",
                    "type": "uint8"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "uint256",
                    "name": "_stableBridgingFee",
                    "type": "uint256"
                },
                {
                    "internalType": "bytes32",
                    "name": "_externalID",
                    "type": "bytes32"
                }
            ],
            "name": "revertBurn",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "uint256",
                    "name": "_stableBridgingFee",
                    "type": "uint256"
                },
                {
                    "internalType": "bytes32",
                    "name": "_internalID",
                    "type": "bytes32"
                },
                {
                    "internalType": "address",
                    "name": "_receiveSide",
                    "type": "address"
                },
                {
                    "internalType": "address",
                    "name": "_oppositeBridge",
                    "type": "address"
                },
                {
                    "internalType": "uint256",
                    "name": "_chainID",
                    "type": "uint256"
                }
            ],
            "name": "revertSynthesizeRequest",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "address",
                    "name": "_fabric",
                    "type": "address"
                }
            ],
            "name": "setFabric",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "contract IMetaRouterV2",
                    "name": "_metaRouter",
                    "type": "address"
                }
            ],
            "name": "setMetaRouter",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
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
                    "name": "_threshold",
                    "type": "uint256"
                }
            ],
            "name": "setTokenThreshold",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "bytes32",
                    "name": "",
                    "type": "bytes32"
                }
            ],
            "name": "synthesizeStates",
            "outputs": [
                {
                    "internalType": "enum Synthesis.SynthesizeState",
                    "name": "",
                    "type": "uint8"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "address",
                    "name": "",
                    "type": "address"
                }
            ],
            "name": "tokenThreshold",
            "outputs": [
                {
                    "internalType": "uint256",
                    "name": "",
                    "type": "uint256"
                }
            ],
            "stateMutability": "view",
            "type": "function"
        },
        {
            "inputs": [
                {
                    "internalType": "address",
                    "name": "newOwner",
                    "type": "address"
                }
            ],
            "name": "transferOwnership",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [],
            "name": "unpause",
            "outputs": [],
            "stateMutability": "nonpayable",
            "type": "function"
        },
        {
            "inputs": [],
            "name": "versionRecipient",
            "outputs": [
                {
                    "internalType": "string",
                    "name": "",
                    "type": "string"
                }
            ],
            "stateMutability": "pure",
            "type": "function"
        }
    ]
"""

internal val synthesizeContractAbi = Json.parseToJsonElement(ABI).jsonArray
