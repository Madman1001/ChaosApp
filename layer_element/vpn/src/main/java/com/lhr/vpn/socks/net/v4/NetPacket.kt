package com.lhr.vpn.socks.net.v4

import com.lhr.vpn.setInt
import com.lhr.vpn.setShort
import com.lhr.vpn.socks.net.PROTO_TCP
import com.lhr.vpn.socks.net.PROTO_UDP
import com.lhr.vpn.toNetInt

/**
 * @CreateDate: 2022/11/2
 * @Author: mac
 * @Description: 网络数据包
 */
class NetPacket(val rawData: ByteArray, val offset: Int = 0) {
    val ipHeader = NetIPHeader(rawData, offset)
    val tcpHeader = NetTcpHeader(rawData, offset + ipHeader.headerLength * 4)
    val udpHeader = NetUdpHeader(rawData, offset + ipHeader.headerLength * 4)

    var data: ByteArray
    init {
        if (isTcp()){
            data = ByteArray(ipHeader.totalLength - (ipHeader.headerLength * 4) - (tcpHeader.headerLength * 4))
            System.arraycopy(rawData, offset + (ipHeader.headerLength * 4) + (tcpHeader.headerLength * 4), data, 0, data.size)
        } else {
            data = ByteArray(udpHeader.totalLength.toNetInt() - 8)
            System.arraycopy(rawData, offset + ipHeader.headerLength * 4 + 8, data, 0, data.size)
        }
    }

    fun isTcp(): Boolean {
        return ipHeader.upperProtocol == PROTO_TCP.toByte()
    }

    fun isUdp(): Boolean {
        return ipHeader.upperProtocol == PROTO_UDP.toByte()
    }

    fun setChecksum() {
        if (isTcp()){
            tcpHeader.checksum = 0
        }
        if (isUdp()){
            udpHeader.checksum = 0
        }
        val checksumData = ByteArray(12 + (ipHeader.totalLength - ipHeader.headerLength * 4))
        checksumData.setInt(0, ipHeader.sourceIp)
        checksumData.setInt(4, ipHeader.destinationIp)
        checksumData.setShort(8, ipHeader.upperProtocol.toUByte().toShort())
        checksumData.setShort(10, (ipHeader.totalLength - ipHeader.headerLength * 4).toShort())
        System.arraycopy(rawData, offset + ipHeader.headerLength * 4, checksumData, 12, (ipHeader.totalLength - ipHeader.headerLength * 4))

        if (isTcp()){
            tcpHeader.checksum = ChecksumUtil.checksum(checksumData, 0, checksumData.size).toShort()
        }
        if (isUdp()){
            udpHeader.checksum = ChecksumUtil.checksum(checksumData, 0, checksumData.size).toShort()
        }
        
        ipHeader.checksum = ChecksumUtil.checksum(ipHeader.rawData, ipHeader.offset, ipHeader.headerLength * 4).toShort()
    }
}