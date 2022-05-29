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
        "name": "BurnCompleted",
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
            }
        ],
        "name": "RevertBurnRequest",
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
        "name": "RevertSynthesizeCompleted",
        "type": "event"
    },
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
        "name": "SynthesizeRequest",
        "type": "event"
    },
    {
        "inputs": [
            {
                "internalType": "address",
                "name": "",
                "type": "address"
            }
        ],
        "name": "balanceOf",
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
                "internalType": "address",
                "name": "_wrapper",
                "type": "address"
            },
            {
                "internalType": "address",
                "name": "_whitelistedToken",
                "type": "address"
            },
            {
                "internalType": "contract IMetaRouter",
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
        "inputs": [],
        "name": "metaRouter",
        "outputs": [
            {
                "internalType": "contract IMetaRouter",
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
                        "name": "rtoken",
                        "type": "address"
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
                        "name": "syntCaller",
                        "type": "address"
                    },
                    {
                        "internalType": "uint256",
                        "name": "chainID",
                        "type": "uint256"
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
                    },
                    {
                        "internalType": "address",
                        "name": "revertableAddress",
                        "type": "address"
                    }
                ],
                "internalType": "struct MetaRouteStructs.MetaSynthesizeTransaction",
                "name": "_metaSynthesizeTransaction",
                "type": "tuple"
            }
        ],
        "name": "metaSynthesize",
        "outputs": [
            {
                "internalType": "bytes32",
                "name": "",
                "type": "bytes32"
            }
        ],
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
                "name": "_externalID",
                "type": "bytes32"
            },
            {
                "internalType": "address",
                "name": "_to",
                "type": "address"
            },
            {
                "internalType": "uint256",
                "name": "_amount",
                "type": "uint256"
            },
            {
                "internalType": "address",
                "name": "_rToken",
                "type": "address"
            },
            {
                "internalType": "address",
                "name": "_finalReceiveSide",
                "type": "address"
            },
            {
                "internalType": "bytes",
                "name": "_finalCalldata",
                "type": "bytes"
            },
            {
                "internalType": "uint256",
                "name": "_finalOffset",
                "type": "uint256"
            }
        ],
        "name": "metaUnsynthesize",
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
                "name": "rtoken",
                "type": "address"
            },
            {
                "internalType": "enum Portal.RequestState",
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
                "name": "_chainId",
                "type": "uint256"
            }
        ],
        "name": "revertBurnRequest",
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
                "name": "_externalID",
                "type": "bytes32"
            }
        ],
        "name": "revertSynthesize",
        "outputs": [],
        "stateMutability": "nonpayable",
        "type": "function"
    },
    {
        "inputs": [
            {
                "internalType": "contract IMetaRouter",
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
                "internalType": "address",
                "name": "_token",
                "type": "address"
            },
            {
                "internalType": "bool",
                "name": "_activate",
                "type": "bool"
            }
        ],
        "name": "setWhitelistToken",
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
        "name": "synthesize",
        "outputs": [
            {
                "internalType": "bytes32",
                "name": "",
                "type": "bytes32"
            }
        ],
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
        "name": "synthesizeNative",
        "outputs": [
            {
                "internalType": "bytes32",
                "name": "",
                "type": "bytes32"
            }
        ],
        "stateMutability": "payable",
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
                "internalType": "bytes",
                "name": "_approvalData",
                "type": "bytes"
            },
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
        "name": "synthesizeWithPermit",
        "outputs": [
            {
                "internalType": "bytes32",
                "name": "",
                "type": "bytes32"
            }
        ],
        "stateMutability": "nonpayable",
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
                "name": "",
                "type": "address"
            }
        ],
        "name": "tokenWhitelist",
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
                "name": "_to",
                "type": "address"
            }
        ],
        "name": "unsynthesize",
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
        "name": "unsynthesizeStates",
        "outputs": [
            {
                "internalType": "enum Portal.UnsynthesizeState",
                "name": "",
                "type": "uint8"
            }
        ],
        "stateMutability": "view",
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
    },
    {
        "inputs": [],
        "name": "wrapper",
        "outputs": [
            {
                "internalType": "address",
                "name": "",
                "type": "address"
            }
        ],
        "stateMutability": "view",
        "type": "function"
    }
]
"""

internal val portalContractAbi = Json.parseToJsonElement(ABI.trimIndent()).jsonArray
