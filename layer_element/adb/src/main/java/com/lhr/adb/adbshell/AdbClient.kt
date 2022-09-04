package com.lhr.adb.adbshell

import android.content.Context
import android.util.Log
import com.cgutman.adblib.AdbConnection
import com.cgutman.adblib.AdbCrypto
import com.cgutman.adblib.AdbStream
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket

/**
 * @author lhr
 * @date 4/9/2022
 * @des adb client
 */
class AdbClient(context: Context) {
    private val tag = this::class.java.simpleName

    private val CONN_TIMEOUT = 5000

    private var pubKeyFile = context.cacheDir.absolutePath + File.pathSeparator + "pub.key"
    private var privKeyFile = context.cacheDir.absolutePath + File.pathSeparator + "priv.key"

    private var crypto: AdbCrypto? = null

    private var sock: Socket? = null

    private var adb: AdbConnection? = null

    var stream: AdbStream? = null

    @Synchronized
    fun openConnect(ip: String, port: Int): AdbStream {
        if (sock != null || adb != null || stream != null) {
            throw RuntimeException("connection is already open")
        }

        crypto = setupCrypto(pubKeyFile, privKeyFile) ?: throw RuntimeException("setup crypto fail!")

        Log.d(tag, "Socket connecting...")

        sock = Socket(ip, port)

       // sock?.connect(InetSocketAddress(ip, port), CONN_TIMEOUT)

        Log.d(tag, "Socket connected")

        adb = AdbConnection.create(sock, crypto)
        Log.d(tag, "ADB connecting...")
        adb?.connect()
        Log.d(tag, "ADB connected")

        stream = adb?.open("shell:")
        stream ?: throw RuntimeException("open adb stream fail")

        return stream!!
    }

    @Synchronized
    fun closeConnect(){
        stream?.close()
        adb?.close()
        sock?.close()

        stream = null
        adb = null
        sock = null
    }

    private fun setupCrypto(pubKeyFile: String, privKeyFile: String): AdbCrypto? {
        val pub = File(pubKeyFile)
        val priv = File(privKeyFile)
        var c: AdbCrypto? = null

        if (pub.exists() && priv.exists()) {
            c = try {
                AdbCrypto.loadAdbKeyPair(AdbClientBase64(), priv, pub)
            } catch (e: Exception) {
                null
            }
        }
        if (c == null) {
            c = AdbCrypto.generateAdbKeyPair(AdbClientBase64())
            c.saveAdbKeyPair(priv, pub)
        }
        return c
    }
}