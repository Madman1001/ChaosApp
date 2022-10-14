package com.lhr.vpn.socks

import android.util.Log
import com.lhr.vpn.channel.StreamChannel
import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.net.v4.NetUdpPacket
import com.lhr.vpn.socks.net.v4.NetV4Protocol
import com.lhr.vpn.util.PacketFactory
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
    private val tun2Socks: Tun2Socks,
    private val udpSocket: DatagramSocket
) {
    private val tag = this::class.java.simpleName

    private val receivePacket = DatagramPacket(ByteArray(1024), 1024)

    private val udpChannel = object : StreamChannel<NetIpPacket>() {
        override fun writeData(o: NetIpPacket) {
            val udpPacket = NetUdpPacket(o.data)
            val packet = makeUdpSocketPacket(udpPacket.data)
            udpSocket.send(packet)

            Log.d(tag, "output ${o.toString()} ${NetUdpPacket(o.data)}")
        }

        override fun readData() {
            udpSocket.receive(receivePacket)
            val data = ByteArray(receivePacket.length)
            System.arraycopy(receivePacket.data, 0, data, 0, data.size)
            val packet = makeUdpTunPacket("This is Tun2Socks".toByteArray())
            tun2Socks.sendData(packet.encodePacket().array())
            Log.d(tag, "input ${packet.toString()} ${NetUdpPacket(packet.data)}")
        }
    }

    init {
        udpChannel.openChannel()
    }

    fun sendTun2Socket(ipPacket: NetIpPacket){
        udpChannel.sendData(ipPacket)
    }

    private fun makeUdpTunPacket(data: ByteArray): NetIpPacket {
        val udpPacket = PacketFactory.createUdpPacket(
            data = data,
            sourcePort = bean.targetPort,
            targetPort = bean.sourcePort
        )
        return PacketFactory.createIpPacket(
            data = udpPacket.encodePacket().array(),
            upperProtocol = NetV4Protocol.PROTO_UDP.toByte(),
            source = bean.targetAddress,
            target = bean.sourceAddress
        )
    }

    private fun makeUdpSocketPacket(data: ByteArray): DatagramPacket {
        val address = InetSocketAddress(bean.targetAddress, bean.targetPort.toUShort().toInt())
        return DatagramPacket(data, data.size, address)
    }
}