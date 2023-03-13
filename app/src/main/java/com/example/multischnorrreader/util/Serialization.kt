package com.example.multischnorrreader.util

import com.example.multischnorrreader.SignState
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger

fun BigInteger.toHex(): String = "0x${toString(16)}"

fun ECPoint.toHex(): Pair<String, String> = normalize().run {
    Pair(
        xCoord.toBigInteger().toHex(),
        yCoord.toBigInteger().toHex()
    )
}

fun SignState.Success.toJson(): String {
    val (Xx, Xy) = group.toHex()
    val m = message.joinToString(separator = "") { it.toUByte().toString(16) }
    val (Rx, Ry) = signature.nonce.toHex()
    val s = signature.value.toHex()

    return """
        {
            "group": {
                "x": "$Xx",
                "y": "$Xy"
            },
            "message": "$m",
            "nonce": {
                "x": "$Rx",
                "y": "$Ry"
            },
            "signature": "$s"
        }
    """.trimIndent()
}