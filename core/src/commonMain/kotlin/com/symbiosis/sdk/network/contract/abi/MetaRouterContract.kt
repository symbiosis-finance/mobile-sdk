package com.symbiosis.sdk.network.contract.abi

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray

//language=json
private const val ABI = """
[
  {
    "inputs": [
      {
        "internalType": "address",
        "name": "_synthesis",
        "type": "address"
      },
      {
        "internalType": "address",
        "name": "_wrapper",
        "type": "address"
      }
    ],
    "stateMutability": "nonpayable",
    "type": "constructor"
  },
  {
    "inputs": [
      {
        "components": [
          {
            "internalType": "address",
            "name": "to",
            "type": "address"
          },
          {
            "internalType": "address[]",
            "name": "firstPath",
            "type": "address[]"
          },
          {
            "internalType": "address[]",
            "name": "secondPath",
            "type": "address[]"
          },
          {
            "internalType": "address[]",
            "name": "finalPath",
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
            "internalType": "uint256",
            "name": "firstAmountOutMin",
            "type": "uint256"
          },
          {
            "internalType": "uint256",
            "name": "secondAmountOutMin",
            "type": "uint256"
          },
          {
            "internalType": "uint256",
            "name": "firstDeadline",
            "type": "uint256"
          },
          {
            "internalType": "uint256",
            "name": "finalAmountOutMin",
            "type": "uint256"
          },
          {
            "internalType": "uint256",
            "name": "finalDeadline",
            "type": "uint256"
          },
          {
            "internalType": "address",
            "name": "finalDexRouter",
            "type": "address"
          },
          {
            "internalType": "uint256",
            "name": "chainID",
            "type": "uint256"
          },
          {
            "internalType": "address",
            "name": "bridge",
            "type": "address"
          },
          {
            "internalType": "address",
            "name": "portal",
            "type": "address"
          }
        ],
        "internalType": "struct MetaRouteStructs.MetaRouteTransaction",
        "name": "_metarouteTransaction",
        "type": "tuple"
      }
    ],
    "name": "metaRoute",
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
        "components": [
          {
            "internalType": "address",
            "name": "to",
            "type": "address"
          },
          {
            "internalType": "address[]",
            "name": "firstPath",
            "type": "address[]"
          },
          {
            "internalType": "address[]",
            "name": "secondPath",
            "type": "address[]"
          },
          {
            "internalType": "address[]",
            "name": "finalPath",
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
            "internalType": "uint256",
            "name": "firstAmountOutMin",
            "type": "uint256"
          },
          {
            "internalType": "uint256",
            "name": "secondAmountOutMin",
            "type": "uint256"
          },
          {
            "internalType": "uint256",
            "name": "firstDeadline",
            "type": "uint256"
          },
          {
            "internalType": "uint256",
            "name": "finalAmountOutMin",
            "type": "uint256"
          },
          {
            "internalType": "uint256",
            "name": "finalDeadline",
            "type": "uint256"
          },
          {
            "internalType": "address",
            "name": "finalDexRouter",
            "type": "address"
          },
          {
            "internalType": "uint256",
            "name": "chainID",
            "type": "uint256"
          },
          {
            "internalType": "address",
            "name": "bridge",
            "type": "address"
          },
          {
            "internalType": "address",
            "name": "portal",
            "type": "address"
          }
        ],
        "internalType": "struct MetaRouteStructs.MetaRouteTransaction",
        "name": "_metarouteTransaction",
        "type": "tuple"
      }
    ],
    "name": "metaRouteNative",
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
    "inputs": [],
    "name": "synthesis",
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
  },
  {
    "stateMutability": "payable",
    "type": "receive"
  }
]
"""

internal val metaRouterContract: JsonArray = Json.parseToJsonElement(ABI).jsonArray

