package com.example.multischnorrreader.crypto

import org.bouncycastle.crypto.CryptoException
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.util.BigIntegers
import java.math.BigInteger
import java.security.SecureRandom

class PhoneCosigner {
    private val rng = SecureRandom()

    private val identityPriv: BigInteger = randomScalar()
    val pk = PossessedKey.fromPrivate(identityPriv)
    val identity = identityPriv * SchemeParameters.ec.g

    private var state: State = State.Initialized
    val group get() = (state as? State.Grouped)?.group

    private fun randomScalar() = BigIntegers.createRandomInRange(
        BigInteger.ONE, SchemeParameters.ec.n, rng
    )

    fun reset() {
        state = State.Initialized
    }

    fun dkgen(cardPk: PossessedKey) {
        if (!cardPk.verify())
            throw CryptoException("Invalid card key PoP")
        state = State.Grouped(cardKey = cardPk.key, group = identityPriv * cardPk.key)
    }

    fun commit(cardNonce: ECPoint): ECPoint {
        val grouped = state as? State.Grouped ?: throw IllegalStateException()
        val noncePriv = randomScalar()
        val groupNonce = noncePriv * SchemeParameters.ec.g + identityPriv * cardNonce
        state = State.Committed(grouped.cardKey, grouped.group, cardNonce, noncePriv, groupNonce)
        return groupNonce
    }

    fun sign(msg: ByteArray, cardSig: BigInteger): Signature {
        return (state as? State.Committed ?: throw IllegalStateException()).run {
            val verify = { nonce: ECPoint ->
                Signature(nonce, cardSig).verify(
                    key = cardKey,
                    msg = msg,
                    challengeKey = group,
                    challengeNonce = groupNonce
                )
            }
            if (!verify(cardNonce)) {
                throw if (verify(-cardNonce)) NegatedNonceException()
                else CryptoException("Invalid card signature")
            }

            val value = (noncePriv + identityPriv * cardSig).mod(SchemeParameters.ec.n)
            state = State.Grouped(cardKey, group)
            Signature(groupNonce, value)
        }
    }

    class NegatedNonceException : Exception()

    sealed class State {
        object Initialized : State()

        open class Grouped(
            val cardKey: ECPoint,
            val group: ECPoint
        ) : State()

        class Committed(
            cardKey: ECPoint,
            group: ECPoint,
            val cardNonce: ECPoint,
            val noncePriv: BigInteger,
            val groupNonce: ECPoint,
        ) : Grouped(cardKey, group)
    }
}