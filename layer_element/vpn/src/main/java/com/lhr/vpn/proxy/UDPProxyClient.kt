package com.lhr.vpn.proxy

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.protocol.UDPPacket
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.*

/**
 * @author lhr
 * @date 2021/12/4
 * @des udp 客户端代理类
 */
class UDPProxyClient(vpnService: VpnService) : Runnable {
    private val tag = "UDPProxyClient"
    private val packetList = Vector<DatagramPacket>()
    private val udpSocket = DatagramSocket()
    private var currentUdpThread: Thread? = null
    init {
        vpnService.protect(udpSocket)
    }

    @Synchronized override fun run() {
        currentUdpThread = Thread.currentThread()
        while (true){
            if (packetList.isNotEmpty()){
                val packet = packetList.removeFirst()
                udpSocket.send(packet)

                val data = ByteArray(1024)
                val dp = DatagramPacket(data, data.size)
                udpSocket.receive(dp)
                val str = String(data, 0, dp.length)
                Log.d(tag, "udp server receive $str")
            }else{
                try {
                    Thread.sleep(Long.MAX_VALUE)
                }catch (e: InterruptedException){
                }
                Log.e(tag,"线程被唤醒")
            }
        }
    }

    fun sendPacket(packet: UDPPacket){
        val buf = packet.getData()
        val address = InetSocketAddress(packet.getHostname(), packet.getTargetPort())
        val udpPacket = DatagramPacket(buf, buf.size)
        Log.e(tag,"hostname ${packet.getHostname()} port ${packet.getTargetPort()}")
        udpPacket.socketAddress = address
        packetList.add(udpPacket)
        currentUdpThread?.interrupt()
    }
}