package com.example.multischnorrreader

import com.example.multischnorrreader.card.AppletCosigner
import com.example.multischnorrreader.crypto.CardCosigner
import com.example.multischnorrreader.crypto.PhoneCosigner
import com.example.multischnorrreader.crypto.SignatureManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CryptoUnitTest {
    private lateinit var phoneCosigner: PhoneCosigner
    private lateinit var signatureManager: SignatureManager
    private lateinit var cardCosigner: CardCosigner

    @Before
    fun setup() {
        phoneCosigner = PhoneCosigner()
        signatureManager = SignatureManager(phoneCosigner)
        cardCosigner = SimulatedAppletCosigner().apply { runBlocking { init() } }
    }

    @Test
    fun group() = runTest {
        val group = signatureManager.group(cardCosigner)
        assertEquals(group, phoneCosigner.group)
        assertEquals(group, cardCosigner.getGroup())
    }

    private suspend fun sign(
        prob: Boolean = false,
        piggy: Boolean = false,
        reps: Int = 512,
        message: ByteArray = ByteArray(AppletCosigner.MSG_LEN),
    ) {
        val group = signatureManager.group(cardCosigner)

        for (i in 1..reps) {
            val signature = signatureManager.sign(
                cardCosigner,
                msg = message,
                prob = prob,
                piggy = piggy,
            ).run { if (prob) last() else first() }

            assertNotNull(signature)
            assertTrue(signature.verify(group, message))
        }
    }

    @Test
    fun signSimple() = runTest { sign() }

    @Test
    fun signProb() = runTest { sign(prob = true) }

    @Test
    fun signPiggy() = runTest { sign(piggy = true) }

    @Test
    fun signProbPiggy() = runTest { sign(prob = true, piggy = true) }
}