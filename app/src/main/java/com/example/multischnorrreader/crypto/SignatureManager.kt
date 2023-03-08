package com.example.multischnorrreader.crypto

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.bouncycastle.math.ec.ECPoint

class SignatureManager(private val phoneCosigner: PhoneCosigner) {
    private var cachedCardNonce: ECPoint? = null

    suspend fun group(cardCosigner: CardCosigner): ECPoint {
        if (cardCosigner.getGroup() != phoneCosigner.group) {
            cachedCardNonce = null
            phoneCosigner.dkgen(cardCosigner.dkgen(phoneCosigner.pk))
        }
        return phoneCosigner.group!!
    }

    private suspend fun trySign(
        cardCosigner: CardCosigner,
        msg: ByteArray,
        prob: Boolean,
        piggy: Boolean
    ): Signature {
        val cardNonce = cachedCardNonce ?: cardCosigner.commit(prob)
        cachedCardNonce = null
        val nonce = phoneCosigner.commit(cardNonce)
        val cardSig = if (piggy) {
            val (cardSig, nextCardNonce) = cardCosigner.signCommit(nonce, msg, prob)
            cachedCardNonce = nextCardNonce
            cardSig
        } else {
            cardCosigner.sign(nonce, msg)
        }
        return phoneCosigner.sign(msg, cardSig)
    }

    suspend fun sign(
        cardCosigner: CardCosigner,
        msg: ByteArray,
        prob: Boolean,
        piggy: Boolean
    ): Flow<Signature?> = flow {
        do {
            val signature = try {
                trySign(cardCosigner, msg, prob, piggy)
            } catch (e: PhoneCosigner.NegatedNonceException) {
                if (prob) null else throw e
            }
            emit(signature)
        } while (signature == null)
    }

    fun reset() {
        cachedCardNonce = null
        phoneCosigner.reset()
    }
}