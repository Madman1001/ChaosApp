package com.lhr.test

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.*
import java.nio.charset.StandardCharsets

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

    fun udpServerTest() {
        GlobalScope.launch(IO) {
            try {
                Log.d(tag, "udpServerTest start")
                val udpServerSocket = DatagramSocket(10086)
                val data = ByteArray(1024)
                val dp = DatagramPacket(data, data.size)
                while (true){
                    udpServerSocket.receive(dp)
                    val str = String(data, 0, dp.length, StandardCharsets.UTF_8)
                    if (str != "exit") {
                        Log.d(tag, dp.address.hostAddress + ":" + dp.port + " -- " + str)
                        val receiveData = str.toByteArray()
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
        GlobalScope.launch {
            try {
                val tcpSocket = Socket(InetAddress.getByName(address), port)
                val output = tcpSocket.getOutputStream()
                output.write(data.toByteArray(StandardCharsets.UTF_8))
                output.flush()
                tcpSocket.shutdownOutput()

                val buffer = ByteArray(1024)
                val len = tcpSocket.getInputStream().read(buffer)
                val str = String(buffer,0,len, StandardCharsets.UTF_8)
                Log.d(tag, "receive: $str")
                tcpSocket.shutdownInput()

                tcpSocket.close()
                Log.d(tag, "over tcp test")
            }catch (e: Exception){

            }
        }
    }

    fun tcpServerTest(){
        GlobalScope.launch {
            try {
                Log.d(tag, "tcpServerSocket start")
                val tcpServerSocket = ServerSocket(10086)
                val socket = tcpServerSocket.accept()
                Log.d(
                    tag,
                    "tcpServerSocket accept ${socket.inetAddress.hostAddress}:${socket.port}"
                )
                val data = ByteArray(1024)
                while (true){
                    val len = socket.getInputStream().read(data, 0, data.size)
                    if (!socket.isOutputShutdown){
                        socket.getOutputStream().write(data, 0, len)
                        socket.getOutputStream().flush()
                    }
                }
                socket.close()
                tcpServerSocket.close()
            }catch (e: Exception){

            }
        }
    }
}