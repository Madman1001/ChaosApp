package com.lhr.vpn.test

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.*

/**
 * @author lhr
 * @date 2021/11/14
 * @des 测试样例
 */
object LocalVpnTest {
    private val mainHandler = Handler(Looper.getMainLooper())
    const val tag = "VpnTest"
    fun httpTest(){
        GlobalScope.launch {
            try {
                val httpTestUrl = URL("http://www.baidu.com")
                val http = httpTestUrl.openConnection() as HttpURLConnection
                mainHandler.postDelayed({
                    http.disconnect()
                    Log.d(tag, "over http test")
                }, 2000)
                http.connect()
            }catch (e: Exception){

            }
        }
    }

    private var udpTestTimes = 0
    fun udpClientTest(){
        GlobalScope.launch {
            try {
                Log.d(tag, "start udp test")
                val buf = "test${++udpTestTimes}".toByteArray()
                val udpSocket = DatagramSocket()
                val address = InetSocketAddress("192.168.4.62", 10086)
                val packet = DatagramPacket(buf, buf.size)
                packet.socketAddress = address
                udpSocket.send(packet)
                udpSocket.close()
                Log.d(tag, "end udp test")
            }catch (e: Exception){

            }
        }
    }

    fun udpServerTest(){
        GlobalScope.launch {
            val port = 10086
            val receive = DatagramSocket(port)
            val data = ByteArray(1024)
            val dp = DatagramPacket(data, data.size)
            Log.d(tag, "udp server is ready ${InetAddress.getLocalHost().hostAddress}:${port}")

            while (true) {
                receive.receive(dp)
                val str = String(data, 0, dp.length)
                if (str != "exit") {
                    Log.d(tag, str)
                    continue
                }
                break
            }
            Log.d(tag, "socket is over!")
            receive.close()
        }
    }

    fun tcpTest(){
        GlobalScope.launch {
            try {
                val buf = "test".toByteArray()
                val tcpSocket = Socket(InetAddress.getByName("14.215.177.39"), 80)
                val os = tcpSocket.getOutputStream()
                os.write(buf)
                tcpSocket.shutdownOutput()
                tcpSocket.close()
                Log.d(tag, "over tcp test")
            }catch (e: Exception){

            }
        }
    }
}