package com.lhr.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.lhr.vpn.handle.VpnProxyHandle
import com.lhr.vpn.protocol.IPPacket
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*


/**
 * @author lhr
 * @date 2021/11/13
 * @des 本地网络代理连接器
 */
class LocalVpnConnection(
    private val vpnService: VpnService,
    private val tunInterface: ParcelFileDescriptor
) {

    companion object {
        private const val TAG = "LocalVpnConnection"

        private const val MAX_PACKET_SIZE = Short.MAX_VALUE.toInt()
    }

    // vpn分层代理入口
    private val vpnHandle = VpnProxyHandle(vpnService, tunInterface)

    private var proxyInputJob: Job? = null

    private var proxyOutputJob: Job? = null

    //发送数据报到VPN通道接口
    private var packetInput: FileInputStream? = null

    //接收数据报到VPN通道接口
    private var packetOutput: FileOutputStream? = null

    // 输出到外部的ip数据包缓存区
    private val packet: ByteBuffer = ByteBuffer.allocate(MAX_PACKET_SIZE)

    // 输入到应用的ip数据包队列
    private val outPacketList: Vector<ByteArray> = Vector<ByteArray>()

    @Synchronized
    fun startProxy() {
        proxyInputJob?.cancel()

        packetInput = FileInputStream(tunInterface.fileDescriptor)

        packetOutput = FileOutputStream(tunInterface.fileDescriptor)

        proxyInputJob = realInputProxy()

        proxyOutputJob = realOutputProxy()
    }

    @Synchronized
    fun stopProxy() {
        try {
            packetInput?.close()
            packetInput = null
            packetOutput?.close()
            packetOutput = null
            proxyInputJob?.cancel()
            proxyInputJob = null
            tunInterface.close()
            Log.e(TAG, "${Thread.currentThread().name} Connection close")
        } catch (e: IOException) {
        }
    }

    private fun realInputProxy(): Job {
        return GlobalScope.launch(CoroutineName("VpnProxy-Input")) {
            try {
                Log.i(TAG, "${Thread.currentThread().name} Vpn Input Starting")
                while (true) {
                    packetInput?.let {
                        readToTun(it)?.let { data ->
                            IPPacket(data)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "${Thread.currentThread().name} Connection failed, exiting", e)
            }
        }
    }

    private fun realOutputProxy(): Job {
        return GlobalScope.launch(CoroutineName("VpnProxy-Output")) {
            try {
                Log.i(TAG, "${Thread.currentThread().name} Vpn Output Starting")
                while (true) {
                    packetInput?.let {
                        readToTun(it)?.let { data ->
                            IPPacket(data)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "${Thread.currentThread().name} Connection failed, exiting", e)
            }
        }
    }

    /**
     * 读取虚拟网卡数据
     */
    private fun readToTun(input: FileInputStream?): ByteArray? {
        synchronized(tunInterface){
            try {
                //读取内部发往外部的数据报
                val len = input?.read(packet.array()) ?: 0
                if (len > 0) {
                    packet.limit(len)
                    //可以进行拦截、修改、转发处理
                    val byteBuffer = ByteArray(len)
                    for (i in 0 until len) {
                        byteBuffer[i] = packet[i]
                    }
                    packet.clear()
                    return byteBuffer
                }
            }catch (e: Exception){

            }
            return null
        }
    }


    /**
     * 写入虚拟网卡数据
     */
    private fun writeToTun(bytes: ByteArray, output: FileOutputStream?): Boolean {
        synchronized(tunInterface){
            try {
                //写入外部发往内部的数据报
                if (output != null){
                    output.write(bytes)
                    return true
                }else{
                    return false
                }
            } catch (e: Exception) {
                return false
            }
        }
    }
}