package com.example.multischnorrreader.card

import com.example.multischnorrreader.crypto.CardCosigner
import com.example.multischnorrreader.crypto.PossessedKey
import com.example.multischnorrreader.crypto.SchemeParameters
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.util.BigIntegers
import java.math.BigInteger

abstract class AppletCosigner : CardCosigner {

    abstract suspend fun init()
    abstract suspend fun finish()

    protected abstract suspend fun transceive(data: ByteArray): ByteArray

    protected suspend fun command(
        cla: Int = CLA,
        ins: Int,
        p1: Int = 0,
        p2: Int = 0,
        data: ByteArray = byteArrayOf(),
    ): ByteArray {
        val header = byteArrayOf(cla.toByte(), ins.toByte(), p1.toByte(), p2.toByte())
        val cmd = if (data.isEmpty()) header else header + data.size.toByte() + data
        val resp = transceive(cmd)
        val status = resp
            .takeLast(2)
            .map { it.toUByte().toInt() }
            .reduce { sw1, sw2 -> (sw1 shl 8) + sw2 }
        if (status != ISO7816.SW_NO_ERROR)
            throw IsoException(status)
        return resp.copyOfRange(0, resp.size - 2)
    }

    // TODO: make this a constant?
    private val pointLen = SchemeParameters.ec.g.getEncoded(false).size
    private val modLen = BigIntegers.getUnsignedByteLength(SchemeParameters.ec.n)

    private fun decodePoint(data: ByteArray) = SchemeParameters.ec.curve.decodePoint(data)

    override suspend fun getIdentity(): ECPoint = decodePoint(command(ins = INS_GET_IDENTITY))

    override suspend fun getGroup(): ECPoint = decodePoint(command(ins = INS_GET_GROUP))

    override suspend fun dkgen(pk: PossessedKey): PossessedKey =
        command(ins = INS_DKGEN, data = pk.key.getEncoded(false) + pk.pop).run {
            PossessedKey(
                key = decodePoint(copyOfRange(0, pointLen)),
                pop = copyOfRange(pointLen, size)
            )
        }

    override suspend fun commit(prob: Boolean): ECPoint =
        decodePoint(command(ins = INS_COMMIT, p1 = if (prob) 1 else 0))

    override suspend fun sign(nonce: ECPoint, msg: ByteArray): BigInteger =
        BigInteger(1, command(ins = INS_SIGN, data = nonce.getEncoded(false) + msg))

    override suspend fun signCommit(
        nonce: ECPoint,
        msg: ByteArray,
        prob: Boolean
    ): Pair<BigInteger, ECPoint> =
        command(
            ins = INS_SIGN_COMMIT,
            p1 = if (prob) 1 else 0,
            data = nonce.getEncoded(false) + msg
        ).run {
            Pair(
                BigInteger(1, copyOfRange(0, modLen)),
                decodePoint(copyOfRange(modLen, size))
            )
        }

    companion object Protocol {
        val AID = "01:ff:ff:04:05:06:07:08:09:01:02"
            .split(":").map { it.toUByte(radix = 16).toByte() }.toByteArray()

        const val CLA = 0x00

        const val INS_GET_IDENTITY = 0x01
        const val INS_GET_GROUP = 0x03
        const val INS_DKGEN = 0x02
        const val INS_COMMIT = 0x04
        const val INS_SIGN = 0x05
        const val INS_SIGN_COMMIT = 0x06

        const val ERR_POP = 0xbad0
        const val ERR_COMMIT = 0xbad1

        const val MSG_LEN = 32
    }
}