package com.example.multischnorrreader.card

import android.nfc.Tag
import android.nfc.tech.IsoDep
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NfcAppletCosigner(
    private val tag: Tag,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) :
    AppletCosigner() {

    private lateinit var tech: IsoDep

    override suspend fun transceive(data: ByteArray): ByteArray = withContext(ioDispatcher) {
        tech.transceive(data)
    }

    override suspend fun init(): Unit = withContext(ioDispatcher) {
        tech = IsoDep.get(tag)
        tech.connect()
        command(ISO7816.CLA_ISO7816, ISO7816.INS_SELECT, p1 = 0x04, data = AID)
    }

    override suspend fun finish() = withContext(ioDispatcher) {
        tech.close()
    }
}