package com.example.multischnorrreader

import com.example.multischnorrreader.crypto.Signature
import org.bouncycastle.math.ec.ECPoint
import kotlin.time.Duration

sealed class SignState {
    object Ready : SignState()

    object Grouping : SignState()

    data class Signing(val attempt: Int) : SignState()

    data class Success(
        val group: ECPoint,
        val message: ByteArray,
        val signature: Signature,
        val duration: Duration,
        val hasCachedNonce: Boolean,
    ) : SignState()

    data class Error(
        val msg: String,
        val hasCachedNonce: Boolean,
    ) : SignState()
}