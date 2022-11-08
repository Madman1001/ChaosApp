package com.lhr.test.vpn

import com.lhr.vpn.socks.net.v4.NetPacket
import com.lhr.vpn.toHexString
import org.junit.Test

/**
 * @CreateDate: 2022/10/12
 * @Author: mac
 * @Description:
 */
class NetTest {

    @Test
    fun ipReadTest(){
        val i = ((4 shl 4) or (5 + 0 / 4)).toByte()

        println(i)
        println(4 shl 4)
        println(5 + 0 / 4)

    }
    private val ipPacketArray = byteArrayOf(
        0x45,
        0x00,
        0x00,
        0x3C,
        0xA0.toByte(),
        0x1B,
        0x40,
        0x00,
        0x40,
        0x06,
        0x16,
        0x0A,
        0xC0.toByte(),
        0xA8.toByte(),
        0x02,
        0x02,
        0xC0.toByte(),
        0xA8.toByte(),
        0x01,
        0x44,
        0x88.toByte(),
        0x5A,
        0x1F,
        0x90.toByte(),
        0x7F,
        0x96.toByte(),
        0x2D,
        0xFC.toByte(),
        0x00,
        0x00,
        0x00,
        0x00,
        0xA0.toByte(),
        0x02,
        0xFF.toByte(),
        0xFF.toByte(),
        0xDE.toByte(),
        0x91.toByte(),
        0x00,
        0x00,
        0x02,
        0x04,
        0x05,
        0xB4.toByte(),
        0x04,
        0x02,
        0x08,
        0x0A,
        0x01,
        0x72,
        0x8D.toByte(),
        0xE7.toByte(),
        0x00,
        0x00,
        0x00,
        0x00,
        0x01,
        0x03,
        0x03,
        0x08
    )
    @Test
    fun checksum(){
        val netPacket = NetPacket()
        netPacket.decodePacket(ipPacketArray, 0, ipPacketArray.size)
        println(ipPacketArray.size)
        println(netPacket.ipHeader)
        println(netPacket.tcpHeader)
        println(String(netPacket.data))
        println("1-----" + ipPacketArray.toHexString())
        println("2-----" + netPacket.tcpHeader.rawData.toHexString())
        println("3-----" + netPacket.tcpHeader.optionData.toHexString())
        assert(netPacket.calculateIpChecksum().toInt() == 0)
        assert(netPacket.calculateTcpChecksum().toInt() == 0)
    }
}