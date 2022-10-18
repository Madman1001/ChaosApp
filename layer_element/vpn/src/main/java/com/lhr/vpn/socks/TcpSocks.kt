package com.lhr.vpn.socks

import android.net.VpnService
import com.lhr.vpn.socks.handle.TcpPacketHandle
import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.net.v4.NetTcpPacket
import com.lhr.vpn.socks.net.v4.NetV4Protocol
import com.lhr.vpn.socks.socket.TcpTunSocket
import com.lhr.vpn.util.PacketV4Factory
import java.net.InetAddress
import java.net.Socket

/**
 * @author lhr
 * @date 15/10/2022
 * @des tun2tcp中转层
 */
class TcpSocks(
    private val vpnService: VpnService,
    private val tun2Socks: Tun2Socks
) {
    private val tag = this::class.java.simpleName

    //tcp 注册表
    private val tcpTunSocketMap by lazy { mutableMapOf<String, TcpTunSocket>() }

    //tcp 数据处理
    private val tcpPacketHandle by lazy { TcpPacketHandle() }

    /**
     * 发送数据
     */
    fun tunToSocks(packet: NetIpPacket) {
        val tcpPacket = NetTcpPacket(packet.data)
        val source = packet.sourceAddress
        val target = packet.targetAddress
        val sourcePort = tcpPacket.sourcePort.toUShort().toInt()
        val targetPort = tcpPacket.targetPort.toUShort().toInt()
        val socket = obtainTcpTunSocket(source, sourcePort, target, targetPort)
        socket.sendPacket(tcpPacket)
    }

    /**
     * 接收数据
     */
    fun socksToTun(bean: NetProxyBean, data: NetTcpPacket) {
        val packet = PacketV4Factory.createIpPacket(
            data = data.encodePacket().array(),
            upperProtocol = NetV4Protocol.PROTO_TCP.toByte(),
            source = bean.targetAddress,
            target = bean.sourceAddress
        )
        tun2Socks.sendData(packet)
    }

    fun closeSocks() {
        for (entry in tcpTunSocketMap) {
            entry.value.socket.close()
        }
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

    private fun obtainTcpTunSocket(
        source: InetAddress,
        sourcePort: Int,
        target: InetAddress,
        targetPort: Int
    ): TcpTunSocket {
        val key = createMapKey(source, sourcePort, target, targetPort)
        var sock = tcpTunSocketMap[key]
        if (sock == null) {
            val bean = NetProxyBean(source, sourcePort, target, targetPort)
            //创建udp socket
            val socket = Socket()
            socket.bind(null)
            vpnService.protect(socket)
            sock = TcpTunSocket(bean, this, socket)
            tcpTunSocketMap[key] = sock
        }
        return sock
    }
}