package com.lhr.vpn.socks.socket

import android.util.Log
import com.lhr.vpn.pool.RunPool
import com.lhr.vpn.pool.TunRunnable
import com.lhr.vpn.socks.NetProxyBean
import com.lhr.vpn.socks.UdpSocks
import com.lhr.vpn.socks.net.v4.NetUdpPacket
import com.lhr.vpn.util.PacketV4Factory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: udp packet 2 Socket
 */
class UdpTunSocket(
    val bean: NetProxyBean,
    private val tunSocks: UdpSocks,
    internal val socket: DatagramSocket
) {
    private val tag = this::class.java.simpleName

    private val receivePacket = DatagramPacket(ByteArray(1024), 1024)

    private var receiveJob: Job? = null

    fun sendPacket(packet: NetUdpPacket){
        Log.d(tag, "sendPacket $packet")

        RunPool.execute(TunRunnable("$tag$this-out"){
            val target = bean.targetAddress
            val targetPort = bean.targetPort
            val udpData = packet.data
            val address = InetSocketAddress(target, targetPort)
            val datagramPacket = DatagramPacket(udpData, udpData.size, address)
            socket.send(datagramPacket)
        })

        if (receiveJob == null || receiveJob?.isActive != true){
            startReceive()
        }
    }

    fun receivePacket(packet: NetUdpPacket){
        Log.d(tag, "receivePacket $packet")

        tunSocks.socksToTun(bean, packet)
    }

    /**
     * 启动接收线程
     */
    private fun startReceive(){
        receiveJob?.cancel()

        val inputRunnable = TunRunnable("$tag$this-in"){
            while (true){
                socket.receive(receivePacket)
                val data = ByteArray(receivePacket.length)
                System.arraycopy(receivePacket.data, 0, data, 0, data.size)
                val udpPacket = PacketV4Factory.createUdpPacket(
                    data = data,
                    sourcePort = bean.targetPort,
                    targetPort = bean.sourcePort
                )
                receivePacket(udpPacket)
            }
        }
        receiveJob = GlobalScope.launch(Dispatchers.IO){
            inputRunnable.run()
        }
    }
}