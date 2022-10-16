package com.lhr.vpn.socks.socks

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.socks.NetProxyBean
import com.lhr.vpn.socks.Tun2Socks
import com.lhr.vpn.socks.handle.UdpPacketHandle
import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.net.v4.NetUdpPacket
import com.lhr.vpn.socks.net.v4.NetV4Protocol
import com.lhr.vpn.util.PacketV4Factory
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress

/**
 * @author lhr
 * @date 15/10/2022
 * @des tun2udp中转层
 */
class UdpSocks(
    private val vpnService: VpnService,
    private val tun2Socks: Tun2Socks
) {
    private val tag = this::class.java.simpleName

    //udp 注册表
    private val udpTunSocketMap by lazy { mutableMapOf<String, UdpTunSocket>() }

    //udp 数据处理
    private val udpPacketHandleList by lazy { UdpPacketHandle() }

    /**
     * 发送数据
     */
    fun tunToSocks(packet: NetIpPacket) {
        val udpPacket = NetUdpPacket(packet.data)
        val udpData = udpPacket.data
        val source = packet.sourceAddress
        val target = packet.targetAddress
        val sourcePort = udpPacket.sourcePort.toUShort().toInt()
        val targetPort = udpPacket.targetPort.toUShort().toInt()
        val socket = obtainUdpTunSocket(source, sourcePort, target, targetPort)

        val address = InetSocketAddress(target, targetPort)
        val datagramPacket = DatagramPacket(udpData, udpData.size, address)
        Log.d(tag, "output $packet $udpPacket")
        socket.sendPacket(datagramPacket)
    }

    /**
     * 接收数据
     */
    fun socksToTun(bean: NetProxyBean, data: ByteArray) {
        val udpPacket = PacketV4Factory.createUdpPacket(
            data = data,
            sourcePort = bean.targetPort,
            targetPort = bean.sourcePort
        )
        val packet = PacketV4Factory.createIpPacket(
            data = udpPacket.encodePacket().array(),
            upperProtocol = NetV4Protocol.PROTO_UDP.toByte(),
            source = bean.targetAddress,
            target = bean.sourceAddress
        )
        Log.d(tag, "input $packet $udpPacket")
        tun2Socks.sendData(packet)
    }

    fun closeSocks() {
        for (entry in udpTunSocketMap) {
            entry.value.socket.close()
        }
    }

    private fun obtainUdpTunSocket(
        source: InetAddress,
        sourcePort: Int,
        target: InetAddress,
        targetPort: Int
    ): UdpTunSocket {
        val bean = NetProxyBean(source, sourcePort, target, targetPort)
        val key = createMapKey(source, sourcePort, target, targetPort)
        var sock = udpTunSocketMap[key]
        if (sock == null) {
            //创建udp socket
            sock = UdpTunSocket(bean, this, DatagramSocket().apply { vpnService.protect(this) })
            udpTunSocketMap[key] = sock
        }
        return sock
    }

    private fun createMapKey(
        source: InetAddress,
        sourcePort: Int,
        target: InetAddress,
        targetPort: Int
    ): String {
        return StringBuilder()
            .append(source.address).append(":").append(sourcePort)
            .append("-")
            .append(target.address).append(":").append(targetPort)
            .toString()
    }
}