package com.lhr.vpn.socks

import android.util.Log
import com.lhr.vpn.channel.StreamChannel
import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.net.v4.NetTcpPacket
import com.lhr.vpn.socks.net.v4.NetV4Protocol
import com.lhr.vpn.util.PacketFactory
import java.net.Socket
import java.nio.ByteBuffer

/**
 * @CreateDate: 2022/10/13
 * @Author: mac
 * @Description: udp packet 2 Socket
 */
class TcpTunSocket(
    val bean: NetProxyBean,
    private val tun2Socks: Tun2Socks,
    private val tcpSocket: Socket
) {
    private val tag = this::class.java.simpleName

    private val socketChannel = tcpSocket.channel

    private val receiveBuffer = ByteBuffer.allocate(1024)

    private val udpChannel = object : StreamChannel<NetIpPacket>() {
        override fun writeData(o: NetIpPacket) {
            val tcpPacket = NetTcpPacket(o.data)
            socketChannel.write(ByteBuffer.wrap(tcpPacket.data))

            Log.d(tag, "output ${o.toString()} ${NetTcpPacket(o.data)}")
        }

        override fun readData() {
            receiveBuffer.rewind()
            val len = socketChannel.read(receiveBuffer)
            val data = ByteArray(len)
            receiveBuffer.rewind()
            receiveBuffer.get(data)
            val packet = makeTcpTunPacket(data)
            tun2Socks.sendData(packet.encodePacket().array())

            Log.d(tag, "input ${packet.toString()} ${NetTcpPacket(packet.data)}")
        }
    }

    init {
        udpChannel.openChannel()
    }

    fun sendTun2Socket(ipPacket: NetIpPacket) {
        udpChannel.sendData(ipPacket)
    }

    private fun makeTcpTunPacket(data: ByteArray): NetIpPacket {
        val udpPacket = PacketFactory.createTcpPacket(
            data = data,
            sourcePort = bean.targetPort,
            targetPort = bean.sourcePort
        )
        return PacketFactory.createIpPacket(
            data = udpPacket.encodePacket().array(),
            upperProtocol = NetV4Protocol.PROTO_TCP.toByte(),
            source = bean.targetAddress,
            target = bean.sourceAddress
        )
    }
}