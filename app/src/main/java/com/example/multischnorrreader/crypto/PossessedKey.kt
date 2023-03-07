package com.example.multischnorrreader.crypto

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.signers.DSADigestSigner
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.math.ec.ECPoint
import java.math.BigInteger

class PossessedKey(val key: ECPoint, val pop: ByteArray) {
    fun verify(): Boolean = createSigner().run {
        init(false, ECPublicKeyParameters(key, SchemeParameters.ec))
        val data = key.getEncoded(false)
        update(data, 0, data.size)
        verifySignature(pop)
    }

    companion object {
        private fun createSigner() = DSADigestSigner(ECDSASigner(), SHA256Digest())

        fun fromPrivate(private: BigInteger): PossessedKey {
            val key = private * SchemeParameters.ec.g
            val pop = createSigner().run {
                init(true, ECPrivateKeyParameters(private, SchemeParameters.ec))
                val data = key.getEncoded(false)
                update(data, 0, data.size)
                generateSignature()
            }
            return PossessedKey(key, pop)
        }
    }
}