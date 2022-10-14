package com.lhr.vpn.util

import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.net.v4.NetTcpPacket
import com.lhr.vpn.socks.net.v4.NetUdpPacket
import java.net.Inet4Address
import kotlin.random.Random

/**
 * @CreateDate: 2022/10/14
 * @Author: mac
 * @Description: 数据包工厂
 */
object PacketFactory {

    fun createIpPacket(
        data: ByteArray,
        upperProtocol: Byte,
        source: Inet4Address,
        target: Inet4Address
    ): NetIpPacket {
        val netIpPacket = NetIpPacket()
        netIpPacket.data = data
        netIpPacket.ipHeader.flag = 2
        netIpPacket.ipHeader.offset_frag = 0
        netIpPacket.ipHeader.time_to_live = 64
        netIpPacket.ipHeader.identification = Random(System.currentTimeMillis()).nextInt().toShort()

        netIpPacket.ipHeader.source_ip_address = source
        netIpPacket.ipHeader.target_ip_address = target
        netIpPacket.ipHeader.upper_protocol = upperProtocol

        return netIpPacket
    }

    fun createUdpPacket(data: ByteArray, sourcePort: Int, targetPort: Int): NetUdpPacket{
        val netUdpPacket = NetUdpPacket()
        netUdpPacket.data = data
        netUdpPacket.udpHeader.source_port = sourcePort.toShort()
        netUdpPacket.udpHeader.target_port = targetPort.toShort()

        return netUdpPacket
    }

    fun createTcpPacket(data: ByteArray, sourcePort: Int, targetPort: Int): NetTcpPacket{
        val netTcpPacket = NetTcpPacket()
        netTcpPacket.data = data
        netTcpPacket.tcpHeader.source_port = sourcePort.toShort()
        netTcpPacket.tcpHeader.target_port = targetPort.toShort()

        return netTcpPacket
    }
}