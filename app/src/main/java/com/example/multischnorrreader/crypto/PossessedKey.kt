package com.example.multischnorrreader.crypto

import org.bouncycastle.math.ec.ECPoint

data class PossessedKey(val key: ECPoint, val pop: ByteArray)
