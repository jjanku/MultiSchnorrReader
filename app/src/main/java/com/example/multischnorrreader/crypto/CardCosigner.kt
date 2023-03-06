package com.example.multischnorrreader.crypto

import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger

interface CardCosigner {
    suspend fun getIdentity(): ECPoint
    suspend fun getGroup(): ECPoint

    suspend fun dkgen(pk: PossessedKey): PossessedKey
    suspend fun commit(prob: Boolean = false): ECPoint
    suspend fun sign(
        nonce: ECPoint,
        msg: ByteArray,
    ): BigInteger

    suspend fun signCommit(
        nonce: ECPoint,
        msg: ByteArray,
        prob: Boolean = false,
    ): Pair<BigInteger, ECPoint>
}