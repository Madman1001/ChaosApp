package com.lhr.vpn.socks.net.v4

import com.lhr.vpn.setInt
import com.lhr.vpn.setShort
import com.lhr.vpn.toHexString
import com.lhr.vpn.toNetInt

/**
 * @CreateDate: 2022/11/2
 * @Author: mac
 * @Description: 网络数据包
 */
class NetPacket {
    var ipHeader = NetIPHeader()
    var tcpHeader = NetTcpHeader()
    var udpHeader = NetUdpHeader()
    var data = ByteArray(0)

    var isValid: Boolean = false
        private set

    fun isTcp(): Boolean {
        return ipHeader.isTcp()
    }

    fun isUdp(): Boolean {
        return ipHeader.isUdp()
    }

    fun calculateIpChecksum(): Short{
        val checksumData = ByteArray(ipHeader.rawData.size + ipHeader.optionData.size)
        System.arraycopy(ipHeader.rawData, 0, checksumData, 0, ipHeader.rawData.size)
        System.arraycopy(ipHeader.optionData, 0, checksumData, ipHeader.rawData.size, ipHeader.optionData.size)
        return ChecksumUtil.checksum(ipHeader.rawData)
    }

    fun calculateTcpChecksum(): Short{
        val checksumData = ByteArray(12 + tcpHeader.rawData.size + tcpHeader.optionData.size + data.size)
        checksumData.setInt(0, ipHeader.sourceIp)
        checksumData.setInt(4, ipHeader.destinationIp)
        checksumData.setShort(8, ipHeader.upperProtocol.toUByte().toShort())
        checksumData.setShort(10, (tcpHeader.rawData.size + tcpHeader.optionData.size + data.size).toShort())
        System.arraycopy(tcpHeader.rawData, 0, checksumData, 12, tcpHeader.rawData.size)
        System.arraycopy(tcpHeader.optionData, 0, checksumData, 12 + tcpHeader.rawData.size, tcpHeader.optionData.size)
        System.arraycopy(data, 0, checksumData, 12 + tcpHeader.rawData.size + tcpHeader.optionData.size, data.size)
        return ChecksumUtil.checksum(checksumData)
    }

    fun calculateUdpChecksum(): Short {
        val checksumData = ByteArray(12 + udpHeader.rawData.size + data.size)
        checksumData.setInt(0, ipHeader.sourceIp)
        checksumData.setInt(4, ipHeader.destinationIp)
        checksumData.setShort(8, ipHeader.upperProtocol.toUByte().toShort())
        checksumData.setShort(10, (udpHeader.rawData.size + data.size).toShort())
        System.arraycopy(udpHeader.rawData, 0, checksumData, 12, udpHeader.rawData.size)
        System.arraycopy(data, 0, checksumData, 12 + udpHeader.rawData.size, data.size)
        return ChecksumUtil.checksum(checksumData)
    }

    fun decodePacket(rawData: ByteArray, offset: Int = 0, length: Int = rawData.size){
        kotlin.runCatching {
            ipHeader.decode(rawData, offset)
            val tempData: ByteArray
            if (ipHeader.isTcp()){
                tcpHeader.decode(rawData, offset + ipHeader.headerLength * 4)
                tempData = ByteArray(length - ipHeader.headerLength * 4 - tcpHeader.headerLength * 4)
                System.arraycopy(rawData, offset + ipHeader.headerLength * 4 + tcpHeader.headerLength * 4, tempData, 0, tempData.size)
            } else if (ipHeader.isUdp()){
                udpHeader.decode(rawData, offset + ipHeader.headerLength * 4)
                tempData = ByteArray(udpHeader.totalLength.toNetInt() - 8)
                System.arraycopy(rawData, offset + ipHeader.headerLength * 4 + 8, tempData, 0, tempData.size)
            } else {
                tempData = ByteArray(0)
            }
            data = tempData
            isValid = true
        }.onFailure {
            isValid = false
        }
    }

    fun encodePacket(resetChecksum: Boolean = true): ByteArray{
        return if (isTcp()){
            if (resetChecksum){
                tcpHeader.checksum = 0
                val tcpTotalLength = tcpHeader.rawData.size + tcpHeader.optionData.size
                if (tcpTotalLength % 4 > 0){
                    tcpHeader.headerLength = (tcpTotalLength / 4) + 1
                } else {
                    tcpHeader.headerLength = tcpTotalLength / 4
                }
                tcpHeader.checksum = calculateTcpChecksum()
                ipHeader.checksum = 0
                val ipTotalLength = ipHeader.rawData.size + ipHeader.optionData.size
                if (ipTotalLength % 4 > 0){
                    ipHeader.headerLength = (ipTotalLength / 4) + 1
                } else {
                    ipHeader.headerLength = ipTotalLength / 4
                }
                ipHeader.totalLength = (ipHeader.headerLength * 4 + tcpHeader.headerLength * 4 + data.size).toShort()
                ipHeader.checksum = calculateIpChecksum()
            }

            val byteArray = ByteArray(ipHeader.totalLength.toNetInt())
            System.arraycopy(ipHeader.rawData, 0, byteArray, 0, ipHeader.rawData.size)
            System.arraycopy(ipHeader.optionData, 0, byteArray, ipHeader.rawData.size, ipHeader.optionData.size)
            System.arraycopy(tcpHeader.rawData, 0, byteArray, ipHeader.headerLength * 4, tcpHeader.rawData.size)
            System.arraycopy(tcpHeader.optionData, 0, byteArray, ipHeader.headerLength * 4 + tcpHeader.rawData.size, tcpHeader.optionData.size)
            System.arraycopy(data, 0, byteArray, ipHeader.headerLength * 4 + tcpHeader.headerLength * 4, data.size)
            byteArray
        } else if (isUdp()){
            if (resetChecksum){
                udpHeader.checksum = 0
                udpHeader.totalLength = (udpHeader.rawData.size + data.size).toShort()
                udpHeader.checksum = calculateUdpChecksum()
                ipHeader.checksum = 0
                val ipTotalLength = ipHeader.rawData.size + ipHeader.optionData.size
                if (ipTotalLength % 4 > 0){
                    ipHeader.headerLength = (ipTotalLength / 4) + 1
                } else {
                    ipHeader.headerLength = ipTotalLength / 4
                }
                ipHeader.totalLength = (ipHeader.headerLength * 4 + udpHeader.totalLength).toShort()
                ipHeader.checksum = calculateIpChecksum()
            }

            val byteArray = ByteArray(ipHeader.totalLength.toNetInt())
            System.arraycopy(ipHeader.rawData, 0, byteArray, 0, ipHeader.rawData.size)
            System.arraycopy(ipHeader.optionData, 0, byteArray, ipHeader.rawData.size, ipHeader.optionData.size)
            System.arraycopy(udpHeader.rawData, 0, byteArray, ipHeader.headerLength * 4, udpHeader.rawData.size)
            System.arraycopy(data, 0, byteArray, ipHeader.headerLength * 4 + 8, data.size)
            byteArray
        } else {
            throw RuntimeException("unknown packet data")
        }
    }
}