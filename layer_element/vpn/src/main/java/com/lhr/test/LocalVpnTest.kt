package com.lhr.test

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.Dispatchers.IO
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

    fun udpClientTest(address: String, port: Int, data: String){
        GlobalScope.launch(IO) {
            try {
                val buf = data.toByteArray()

                Log.d(tag, "udp test send $data")

                val udpSocket = DatagramSocket()
                udpSocket.soTimeout = 5000
                val address = InetSocketAddress(address, port)
                val packet = DatagramPacket(buf, buf.size)
                packet.socketAddress = address
                udpSocket.send(packet)

                val data = ByteArray(1024)
                val dp = DatagramPacket(data, data.size)
                udpSocket.receive(dp)
                val str = String(dp.data, 0, dp.length)
                Log.d(tag, "udp test receive $str")

                udpSocket.close()
            }catch (e: Exception){
                Log.d(tag, "udp test Exception ", e)
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
                val str = String(dp.data, 0, dp.length)
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

    fun tcpTest(address: String, port: Int){
        GlobalScope.launch {
            try {
                val buf = "test".toByteArray()
                val tcpSocket = Socket(InetAddress.getByName(address), port)
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