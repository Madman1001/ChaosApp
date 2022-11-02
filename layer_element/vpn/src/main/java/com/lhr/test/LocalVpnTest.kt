package com.lhr.test

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.lhr.common.ext.readText
import com.lhr.common.utils.NetworkUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import java.net.*
import java.nio.charset.StandardCharsets

/**
 * @author lhr
 * @date 2021/11/14
 * @des 测试样例
 */
object LocalVpnTest {
    private val SP_NAME = this::class.java.simpleName + "_sp"

    private val mainHandler = Handler(Looper.getMainLooper())
    const val tag = "VpnTest"

    private var app: Application? = null
    private var sp: SharedPreferences? = null

    private const val IP_KEY = "SP_IP_KEY"

    fun initManager(application: Application){
        app = application
    }

    fun getTestIp(): String {
        if (sp == null){
            sp = app?.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        }
        return sp?.getString(IP_KEY, "192.168.2.249") ?: "192.168.2.249"
    }

    fun setTestIp(ip: String){
        if (sp == null){
            sp = app?.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        }
        sp?.edit()?.putString(IP_KEY, ip)?.apply()
    }

    fun httpTest(){
        GlobalScope.launch(IO) {
            try {
                val time = System.currentTimeMillis()
                val httpTestUrl = URL("http://192.168.2.249:8080/myServer")
                val http = httpTestUrl.openConnection() as HttpURLConnection
                http.connect()
                val input = http.inputStream
                val str = input.readText()
                Log.e(tag, "http receive $str")
                http.disconnect()
                Log.d(tag, "over http test, cost ${System.currentTimeMillis() - time}ms")
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun httpsTest(){
        GlobalScope.launch(IO) {
            try {
                val time = System.currentTimeMillis()
                val httpTestUrl = URL("http://www.baidu.com")
                val http = httpTestUrl.openConnection() as HttpURLConnection
                http.connect()
                val input = http.inputStream
                val str = input.readText()
                Log.e(tag, "https receive $str")
                http.disconnect()
                Log.d(tag, "over https test, cost ${System.currentTimeMillis() - time}ms")
            }catch (e: Exception){

            }
        }
    }

    fun udpClientTest(address: String, port: Int, data: String){
        LocalVpnTest.setTestIp(address)

        GlobalScope.launch(IO) {
            try {
                val udpSocket = DatagramSocket()
                GlobalScope.launch {
                    val buffer = ByteArray(1024)
                    while (true){
                        kotlin.runCatching {
                            val dp = DatagramPacket(buffer, buffer.size)
                            udpSocket.receive(dp)
                            val str = String(dp.data, 0, dp.length)
                            withContext(Dispatchers.Main){
                                Toast.makeText(app, str, Toast.LENGTH_SHORT).show()
                            }
                            Log.d(tag, "udp test receive $str")
                        }
                    }
                }

                val addr = InetSocketAddress(address, port)
                Log.d(tag, "udp test send to ${addr.address.hostAddress}:${udpSocket.localPort}\n $data")
                for (i in 0..10){
                    val buf = ("port:${udpSocket.localPort} $data----$i").toByteArray()
                    val packet = DatagramPacket(buf, buf.size)
                    packet.socketAddress = addr
                    udpSocket.send(packet)
                    delay(1000)
                }

                delay(2000)
                udpSocket.close()
            }catch (e: Exception){
                Log.d(tag, "udp test Exception ", e)
            }
        }
    }

    fun udpServerTest() {
        GlobalScope.launch(IO) {
            val tag = "udpServer"
            try {
                val udpServerSocket = DatagramSocket(10086)

                Log.d(tag, "udpServerTest start ${NetworkUtils.getHostIp()}:${udpServerSocket.localPort}")
                val data = ByteArray(1024)
                val dp = DatagramPacket(data, data.size)
                while (true){
                    udpServerSocket.receive(dp)
                    val str = String(data, 0, dp.length, StandardCharsets.UTF_8)
                    if (str != "exit") {
                        withContext(Dispatchers.Main){
                            Toast.makeText(app, str, Toast.LENGTH_SHORT).show()
                        }
                        Log.d(tag, dp.address.hostAddress + ":" + dp.port + " -- " + str)
                        val receiveData = "server ok".toByteArray()
                        val receivePacket = DatagramPacket(receiveData, receiveData.size)
                        receivePacket.socketAddress = dp.socketAddress
                        udpServerSocket.send(receivePacket)
                        continue
                    }
                    break
                }
                udpServerSocket.close()
                Log.d(tag, "over udp test")
            }catch (e: Exception){

            }
        }
    }

    fun tcpClientTest(address: String, port: Int, data: String){
        setTestIp(address)

        GlobalScope.launch(IO) {
            try {
                val tcpSocket = Socket(InetAddress.getByName(address), port)
                val output = tcpSocket.getOutputStream()
                GlobalScope.launch {
                    val buffer = ByteArray(1024)
                    val inputStream = tcpSocket.getInputStream()
                    kotlin.runCatching {
                        while (!tcpSocket.isInputShutdown){
                            val len = inputStream.read(buffer)
                            if (len == -1) break
                            if (len > 0){
                                val str = String(buffer,0,len, StandardCharsets.UTF_8)
                                Log.d(tag, "receive: $str")
                                withContext(Dispatchers.Main){
                                    Toast.makeText(app, str, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }.onFailure {
                        it.printStackTrace()
                        tcpSocket.close()
                    }

                }
                GlobalScope.launch {
                    kotlin.runCatching {
                        for (i in 0..10){
                            output.write(("$data----$i").toByteArray(StandardCharsets.UTF_8))
                            output.flush()
                            delay(1000)
                        }
                        tcpSocket.shutdownOutput()
                        tcpSocket.close()
                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
            }finally {
                Log.d(tag, "over tcp test")
            }
        }
    }

    fun tcpServerTest(){
        GlobalScope.launch(IO){
            try {
                val tcpServerSocket = ServerSocket(10086)

                Log.d(tag, "tcpServerSocket start: ${NetworkUtils.getHostIp()}:${tcpServerSocket.localPort}")

                val socket = tcpServerSocket.accept()
                Log.d(
                    tag,
                    "tcpServerSocket accept ${socket.inetAddress.hostAddress}:${socket.port}"
                )
                val data = ByteArray(1024)
                val input = socket.getInputStream()
                val output = socket.getOutputStream()
                while (true){
                    val len = input.read(data)
                    if (len > 0){
                        val str = String(data,0,len, StandardCharsets.UTF_8)
                        Log.d(
                            tag,
                            "tcpServerSocket >>> $str"
                        )
                        output.write("server ok".toByteArray())
                        output.flush()
                    }
                }
                tcpServerSocket.close()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}