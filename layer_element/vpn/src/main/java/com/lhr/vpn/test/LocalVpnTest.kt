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
                    Log.d(tag,"over http test")
                },2000)
                http.connect()
            }catch (e: Exception){

            }
        }
    }

    fun udpTest(){
        GlobalScope.launch {
            try {
                val buf = "test".toByteArray()
                val udpSocket = DatagramSocket()
                val address = InetAddress.getByName("14.215.177.39")
                val packet = DatagramPacket(buf,buf.size,address,4445)
                udpSocket.send(packet)
                udpSocket.close()
                Log.d(tag,"over udp test")
            }catch (e: Exception){

            }
        }
    }

    fun tcpTest(){
        GlobalScope.launch {
            try {
                val buf = "test".toByteArray()
                val tcpSocket = Socket(InetAddress.getByName("14.215.177.39"),80)
                val os = tcpSocket.getOutputStream()
                os.write(buf)
                tcpSocket.shutdownOutput()
                tcpSocket.close()
                Log.d(tag,"over tcp test")
            }catch (e: Exception){

            }
        }
    }
}