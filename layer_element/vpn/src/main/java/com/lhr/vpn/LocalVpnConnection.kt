package com.lhr.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.lhr.vpn.handle.NetworkProxyHandle
import com.lhr.vpn.handle.TransportProxyHandle
import com.lhr.vpn.handle.VpnProxyHandle
import com.lhr.vpn.net.v4.NetIpPacket
import com.lhr.vpn.net.v4.NetTcpPacket
import com.lhr.vpn.net.v4.NetUdpPacket
import com.lhr.vpn.protocol.IProtocol
import com.lhr.vpn.util.ByteLog
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
): VpnProxyHandle() {

    companion object {
        private const val TAG = "LocalVpnConnection"

        private const val MAX_PACKET_SIZE = 65535
    }

    private var proxyInputJob: Thread? = null

    private var proxyOutputJob: Thread? = null

    //发送数据报到VPN通道接口
    private var packetInput: FileInputStream? = null

    //接收数据报到VPN通道接口
    private var packetOutput: FileOutputStream? = null

    // 输出到外部的ip数据包缓存区
    private val packet: ByteBuffer = ByteBuffer.allocate(MAX_PACKET_SIZE)

    // 输入到应用的ip数据包队列
    private val outPacketList: Vector<ByteArray> = Vector<ByteArray>()

    @Volatile
    private var isRunning = false

    init {
        val networkHandle = NetworkProxyHandle(vpnService)
        val transportHandle = TransportProxyHandle(vpnService)
        networkHandle.addHandle(transportHandle)
        this.addHandle(networkHandle)
    }

    @Synchronized
    fun startProxy() {
        isRunning = true

        packetInput = FileInputStream(tunInterface.fileDescriptor)

        packetOutput = FileOutputStream(tunInterface.fileDescriptor)

        proxyInputJob = realInputProxy()
        proxyInputJob?.start()

        proxyOutputJob = realOutputProxy()
        proxyOutputJob?.start()

    }

    @Synchronized
    fun stopProxy() {
        try {
            packetInput?.close()
            packetInput = null
            packetOutput?.close()
            packetOutput = null

            isRunning = false
            proxyInputJob = null
            proxyOutputJob = null

            tunInterface.close()
            Log.e(TAG, "${Thread.currentThread().name} Connection close")
        } catch (e: IOException) {
        }
    }

    private fun realInputProxy(): Thread {
        return Thread({
            try {
                Log.i(TAG, "${Thread.currentThread().name} Starting")
                while (isRunning) {
                    //读取输入数据
                    val len = readToTun(packet, packetInput)
                    if (len > 0) {
                        packet.rewind()
                        packet.limit(len)
                        val byteBuffer = ByteArray(len)
                        Log.d(TAG, "虚拟网卡读取:${len}byte")

                        packet.get(byteBuffer)
                        val headerLength = (byteBuffer[0].toUByte().toInt() and 0x0f)
                        //非ip v4 包
                        if (headerLength <= 0){
                            Log.d(TAG, "This is no ipv4 packet: ${ByteLog.binaryToString(byteBuffer)}")
                            packet.clear()
                            continue
                        }

                        //可以进行拦截、修改、转发处理
                        val ipPacket = NetIpPacket(byteBuffer)
                        Log.d(TAG, "in ip packet: $ipPacket")
                        if (ipPacket.isUdp()){
                            val udpPacket = NetUdpPacket(ipPacket.data)
                            Log.d(TAG, "in udp packet: $udpPacket")
                        }
                        if (ipPacket.isTcp()){
                            val tcpPacket = NetTcpPacket(ipPacket.data)
                            Log.d(TAG, "in tcp packet: $tcpPacket")
                        }
//                        if (ipPacket.isValid()) {
//                            Log.d(TAG, "in packet: ${ByteLog.binaryToString(byteBuffer)}")
//                            this.inputData(ipPacket)
//                        }
                        packet.clear()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "${Thread.currentThread().name} exception, exiting", e)
            }
        }, "VpnProxy-Input")
    }

    private fun realOutputProxy(): Thread {
        return Thread({
            try {
                Log.d(TAG, "${Thread.currentThread().name} Starting")
                while (isRunning) {
                    if (outPacketList.isNotEmpty()){
                        val packet = outPacketList.removeFirst()
                        if (packet.isNotEmpty()){
                            Log.d(TAG, "虚拟网卡写入:${packet.size}byte")
                            Log.d(TAG, "out packet: ${ByteLog.binaryToString(packet)}")
                            writeToTun(packet,packetOutput)
                        }
                    }else{
                        try {
                            Thread.sleep(Long.MAX_VALUE)
                        }catch (e: InterruptedException){
                        }
                        Log.e(TAG,"线程被唤醒")
                    }

                }
            } catch (e: Exception) {
                Log.e(TAG, "${Thread.currentThread().name} exception, exiting", e)
            }
        }, "VpnProxy-Output")
    }

    /**
     * 读取虚拟网卡数据
     */
    private fun readToTun(buffer: ByteBuffer, input: FileInputStream?): Int {
        synchronized(tunInterface){
            try {
                //读取内部发往外部的数据报
                return input?.read(buffer.array()) ?: 0
            }catch (e: Exception){
                return -1
            }
        }
    }


    /**
     * 写入虚拟网卡数据
     */
    private fun writeToTun(bytes: ByteArray, output: FileOutputStream?): Int {
        synchronized(tunInterface){
            try {
                //写入外部发往内部的数据报
                if (output != null){
                    output.write(bytes)
                    return 0
                }else{
                    return -1
                }
            } catch (e: Exception) {
                return -1
            }
        }
    }

    override fun onInput(data: IProtocol): IProtocol {
        return data
    }

    override fun onOutput(data: IProtocol): IProtocol? {
        outPacketList.add(data.getRawData())
        if (proxyOutputJob?.state == Thread.State.TIMED_WAITING){
            proxyOutputJob?.interrupt()
        }
        return null
    }
}