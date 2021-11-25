package com.lhr.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.lhr.vpn.protocol.IPPacket
import com.lhr.vpn.util.ByteLog
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer


/**
 * @author lhr
 * @date 2021/11/13
 * @des 本地网络代理连接器
 */
class LocalVpnConnection(
    private val vpnService: VpnService,
    private val tunInterface: ParcelFileDescriptor
) : Runnable {

    companion object{
        private const val TAG = "LocalVpnConnection"

        private const val MAX_PACKET_SIZE = Short.MAX_VALUE.toInt()
    }
    override fun run() {
        try {
            Log.i(TAG, "${Thread.currentThread().name} Starting")

            //发送数据报到VPN通道接口
            val packetInput = FileInputStream(tunInterface.fileDescriptor)
            //接收数据报到VPN通道接口
            val packetOutput = FileOutputStream(tunInterface.fileDescriptor)

            // Allocate the buffer for a single packet.
            val packet: ByteBuffer = ByteBuffer.allocate(MAX_PACKET_SIZE)

            //最近发送数据报的时间
            var lastSendTime = System.currentTimeMillis()
            //最近接收数据报的时间
            var lastReceiveTime = System.currentTimeMillis()

            while (true){
                //读取内部发往外部的数据报
                val len = packetInput.read(packet.array())
                if (len > 0){
                    packet.limit(len)
                    //可以进行拦截、修改、转发处理
                    val byteBuffer = ByteArray(len)
                    for(i in 0 until len){
                        byteBuffer[i] = packet[i]
                    }
                    Log.i(TAG, "${Thread.currentThread().name} send packet ${ByteLog.toByteBufferString(byteBuffer)}")
                    IPPacket(byteBuffer)
                    packet.clear()
                }
                //读取外部发往内部的数据报，（如果有的话）
            }
        } catch (e: IOException) {
            Log.e(TAG, "${Thread.currentThread().name} Connection failed, exiting", e)
        } catch (e: InterruptedException) {
            Log.e(TAG, "${Thread.currentThread().name} Connection failed, exiting", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "${Thread.currentThread().name} Connection failed, exiting", e)
        }
    }

    fun close() {
        try {
            tunInterface.close()
            Log.e(TAG, "${Thread.currentThread().name} Connection close")
        }catch (e: IOException){
        }
    }
}