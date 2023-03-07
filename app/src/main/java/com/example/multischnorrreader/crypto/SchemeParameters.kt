package com.example.multischnorrreader.crypto

import org.bouncycastle.asn1.x9.ECNamedCurveTable
import org.bouncycastle.crypto.params.ECDomainParameters

object SchemeParameters {
    val ec = ECDomainParameters(ECNamedCurveTable.getByName("secp256k1"))
}