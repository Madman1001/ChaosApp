package com.lhr.vpn.socks.net.v4

import com.lhr.vpn.*
import com.lhr.vpn.socks.net.PROTO_TCP
import com.lhr.vpn.socks.net.PROTO_UDP

/**
 * @CreateDate: 2022/10/12
 * @Author: mac
 * @Description: ip数据包解析
 */
class NetIPHeader(val rawData: ByteArray, val offset: Int = 0) {

    //版本号 4 bit ----- [0] and 0xf0 ushr 4
    var version: Int
        get() = ((rawData[offset].toNetInt()) and 0xf0) ushr 4
        set(value) {
            rawData[offset] = ((value shl 4) and 0xf0 or rawData[offset].toNetInt()).toByte()
        }

    //头长度 4 bit (单位 32 bit) ----- [0] and 0x0f
    var headerLength: Int
        get() = (rawData[offset].toNetInt() and 0x0f)
        set(value) {
            rawData[offset] = ((value and 0x0f) or rawData[offset].toNetInt()).toByte()
        }

    //服务类型 8 bit ----- [1]
    var typeOfService: Byte
        get() = rawData[offset + 1]
        set(value) {
            rawData[offset + 1] = value
        }

    //总长度 16 bit (单位 8 bit) ----- [2-3]
    var totalLength: Short
        get() = rawData.getShort(offset + 2)
        set(value) {
            rawData.setShort(offset + 2, value)
        }

    //标识 16 bit ----- [4-5]
    var identification: Short
        get() = rawData.getShort(offset + 4)
        set(value) {
            rawData.setShort(offset + 4, value)
        }


    //标志 3 bit R　DF　MF
    //片偏移 13 bit (记录分片在原报文中的相对位置，以8个字节为偏移单位)
    // ----- [6-7]
    var flagAndOffsetFrag: Short
        get() = rawData.getShort(offset + 6)
        set(value) {
            rawData.setShort(offset + 6, value)
        }

    //生存时间 8 bit ----- [8]
    var timeToLive: Byte
        get() = rawData[offset + 8]
        set(value) {
            rawData[offset + 8] = value
        }

    //上层协议 8 bit ----- [9]
    var upperProtocol: Byte
        get() = rawData[offset + 9]
        set(value) {
            rawData[offset + 9] = value
        }

    //头部校验和 16 bit ----- [10-11]
    var checksum: Short
        get() = rawData.getShort(offset + 10)
        set(value) {
            rawData.setShort(offset + 10, value)
        }

    //源ip地址 32 bit ----- [12-15]
    var sourceIp: Int
        get() = rawData.getInt(offset + 12)
        set(value) {
            rawData.setInt(offset + 12, value)
        }

    //目标ip地址 32 bit ----- [16-19]
    var destinationIp: Int
        get() = rawData.getInt(offset + 16)
        set(value) {
            rawData.setInt(offset + 16, value)
        }

    //可选字段 (单位 32 bit)
    var optionData: ByteArray = ByteArray(0)
        get() {
            val optionByteLength = headerLength * 4 - 20
            val tempData = ByteArray(optionByteLength)
            System.arraycopy(rawData, 20, tempData, 0, optionByteLength)
            return tempData
        }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("IPv4 Packet {")
            .append("\n  Version:        ").append(version)
            .append("\n  Header length:  ").append(headerLength)
            .append("\n  Type:           ").append(typeOfService)
            .append("\n  Total length:   ").append(totalLength)
            .append("\n  Identification: ").append(identification)
            .append("\n  Flags + offset: ").append(flagAndOffsetFrag)
            .append("\n  Time to live:   ").append(timeToLive)
            .append("\n  Protocol:       ").append(upperProtocol)
            .append("\n  Source:         ").append(sourceIp.toIpString())
            .append("\n  Destination:    ").append(destinationIp.toIpString())
            .append("\n  Options: [")
            .append(optionData.toHexString())
            .append("\n  ]")
            .append("\n }")
        return sb.toString()
    }
}