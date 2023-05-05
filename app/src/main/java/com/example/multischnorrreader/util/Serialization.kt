package com.example.multischnorrreader.util

import com.example.multischnorrreader.SignState
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger

fun ECPoint.coords(): Pair<BigInteger, BigInteger> = normalize().run {
    Pair(xCoord.toBigInteger(), yCoord.toBigInteger())
}

fun ByteArray.toHex() = joinToString(separator = "") {
    it.toUByte().toString(16).padStart(2, '0')
}

fun SignState.Success.toJson(): String {
    val (Xx, Xy) = group.coords()
    val m = message.toHex()
    val (Rx, Ry) = signature.nonce.coords()
    val s = signature.value

    return """
        {
            "group": {
                "x": $Xx,
                "y": $Xy
            },
            "message": "$m",
            "nonce": {
                "x": $Rx,
                "y": $Ry
            },
            "signature": $s
        }
    """.trimIndent()
}