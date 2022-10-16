package com.lhr.vpn.util

import com.lhr.vpn.socks.net.v4.NetIpPacket
import com.lhr.vpn.socks.net.v4.NetTcpPacket
import com.lhr.vpn.socks.net.v4.NetUdpPacket
import java.net.Inet4Address
import java.net.InetAddress
import kotlin.random.Random

/**
 * @CreateDate: 2022/10/14
 * @Author: mac
 * @Description: 数据包工厂
 */
object PacketV4Factory {

    fun createIpPacket(
        data: ByteArray,
        upperProtocol: Byte,
        source: InetAddress,
        target: InetAddress
    ): NetIpPacket {
        val netIpPacket = NetIpPacket()
        netIpPacket.data = data
        netIpPacket.flag = 2
        netIpPacket.offsetFrag = 0
        netIpPacket.timeToLive = 64
        netIpPacket.identification = Random(System.currentTimeMillis()).nextInt().toShort()

        netIpPacket.sourceAddress = source
        netIpPacket.targetAddress = target
        netIpPacket.upperProtocol = upperProtocol

        return netIpPacket
    }

    fun createUdpPacket(data: ByteArray, sourcePort: Int, targetPort: Int): NetUdpPacket{
        val netUdpPacket = NetUdpPacket()
        netUdpPacket.data = data
        netUdpPacket.sourcePort = sourcePort.toShort()
        netUdpPacket.targetPort = targetPort.toShort()

        return netUdpPacket
    }

    fun createTcpPacket(data: ByteArray, sourcePort: Int, targetPort: Int): NetTcpPacket{
        val netTcpPacket = NetTcpPacket()
        netTcpPacket.data = data
        netTcpPacket.sourcePort = sourcePort.toShort()
        netTcpPacket.targetPort = targetPort.toShort()

        return netTcpPacket
    }
}