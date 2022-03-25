package com.symbiosis.sdk

import com.symbiosis.sdk.wallet.Credentials
import com.symbiosis.sdk.wallet.KeyPhrase
import dev.icerock.moko.web3.WalletAddress
import dev.icerock.moko.web3.hex.Hex32String


val denWalletAddress: WalletAddress = WalletAddress(
    value = "0x140e21FcFB1E602A1626198d3DbBB58087b59b4E"
)
val alexWalletAddress: WalletAddress = WalletAddress(
    value = "0xC43d2c472cF882e0B190063D66EE8Ce78Bf54dA1"
)

val markWalletAddress: WalletAddress = WalletAddress(
    value = "0xc30C0c0665A9e6c053006bB4E929A62be64D9ACb"
)

val denWalletKeyPhrase = KeyPhrase.wrapChecked(
    keyPhrase = "click air mass have debris relief develop better jacket fitness cake bottom"
)!!

val alexCredentials = Credentials.createFromPrivateKey(
    key = Hex32String(string = "dcbbe2d86c9b024fe253b4f79799471a6c349d1d7d209516d7a93410808e743a")
)

val markWalletKeyPhrase = KeyPhrase.wrapChecked(
   keyPhrase = "aerobic assume amused buffalo museum obey shift dial bundle october flower gown"
)!!

val markCredentials = Credentials.createFromKeyPhrase(markWalletKeyPhrase)

val denCredentials = Credentials.createFromKeyPhrase(denWalletKeyPhrase)
