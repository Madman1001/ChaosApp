package com.lhr.vpn.socks

import android.net.VpnService
import android.util.Log
import com.lhr.vpn.socks.handle.UdpPacketHandle
import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.net.v4.NetUdpPacket
import com.lhr.vpn.socks.net.v4.NetV4Protocol
import com.lhr.vpn.socks.socket.UdpTunSocket
import com.lhr.vpn.util.PacketV4Factory
import java.net.DatagramSocket
import java.net.InetAddress

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
        val source = packet.sourceAddress
        val target = packet.targetAddress
        val sourcePort = udpPacket.sourcePort.toUShort().toInt()
        val targetPort = udpPacket.targetPort.toUShort().toInt()
        val socket = obtainUdpTunSocket(source, sourcePort, target, targetPort)

        socket.sendPacket(udpPacket)
    }

    /**
     * 接收数据
     */
    fun socksToTun(bean: NetProxyBean, data: NetUdpPacket) {
        val packet = PacketV4Factory.createIpPacket(
            data = data.encodePacket().array(),
            upperProtocol = NetV4Protocol.PROTO_UDP.toByte(),
            source = bean.targetAddress,
            target = bean.sourceAddress
        )
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
            .append(source.hostAddress).append(":").append(sourcePort)
            .append("-")
            .append(target.hostAddress).append(":").append(targetPort)
            .toString()
    }
}