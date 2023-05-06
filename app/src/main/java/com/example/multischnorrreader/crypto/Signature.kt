package com.example.multischnorrreader.crypto

import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger
import java.security.MessageDigest

data class Signature(val nonce: ECPoint, val value: BigInteger) {
    fun verify(
        key: ECPoint,
        msg: ByteArray,
        challengeKey: ECPoint = key,
        challengeNonce: ECPoint = nonce
    ): Boolean {
        val challenge = MessageDigest.getInstance("SHA-256").run {
            update(challengeNonce.getEncoded(false))
            update(challengeKey.getEncoded(false))
            update(msg)
            BigInteger(1, digest())
        }
        return value * SchemeParameters.ec.g == nonce + challenge * key
    }
}