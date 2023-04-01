package com.example.multischnorrreader

import com.example.multischnorrreader.card.AppletCosigner
import com.licel.jcardsim.smartcardio.CardSimulator
import com.licel.jcardsim.utils.AIDUtil
import java.io.File

class SimulatedAppletCosigner : AppletCosigner() {
    private val simulator = CardSimulator()

    override suspend fun transceive(data: ByteArray): ByteArray =
        simulator.transmitCommand(data)

    override suspend fun init() {
        // Otherwise requires modulus to be composite
        System.setProperty("com.licel.jcardsim.bouncycastle.rsa.allow_unsafe_mod", "true");
        // Set to seed RandomData from the SecureRandom
        System.setProperty("com.licel.jcardsim.randomdata.secure", "1");

        val aid = AIDUtil.create(AID)
        val jar = File(APPLET_JAR_PATH).readBytes()
        with(simulator) {
            installApplet(aid, APPLET_CLASS_NAME, jar, byteArrayOf(), 0, 0)
            selectApplet(aid)
        }
    }

    override suspend fun finish() {}

    companion object {
        const val APPLET_JAR_PATH = "src/test/java/com/example/multischnorrreader/applet.jar"
        const val APPLET_CLASS_NAME = "applet.MainApplet"
    }
}