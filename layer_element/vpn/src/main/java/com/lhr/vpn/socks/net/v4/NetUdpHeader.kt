package com.lhr.vpn.socks.net.v4

import com.lhr.vpn.getShort
import com.lhr.vpn.setShort

/**
 * @CreateDate: 2022/10/12
 * @Author: mac
 * @Description: udp数据包解析
 */
class NetUdpHeader {
    val rawData: ByteArray = ByteArray(8)
    val offset: Int = 0

    //源端口号 16 bit ----- [0-1]
    var sourcePort: Short
        get() = rawData.getShort(offset)
        set(value) {
            rawData.setShort(offset, value)
        }

    //目标端口号 16 bit ----- [2-3]
    var destinationPort: Short
        get() = rawData.getShort(offset + 2)
        set(value) {
            rawData.setShort(offset + 2, value)
        }

    //UDP长度(单位为：字节) 16 bit ----- [4-5]
    var totalLength: Short
        get() = rawData.getShort(offset + 4)
        set(value) {
            rawData.setShort(offset + 4, value)
        }

    //UDP校验和 16 bit ----- [6-7]
    var checksum: Short
        get() = rawData.getShort(offset + 6)
        set(value) {
            rawData.setShort(offset + 6, value)
        }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("UDP Header {")
            .append("\n  Src:            ").append(sourcePort.toUShort())
            .append("\n  Dst:            ").append(destinationPort.toUShort())
            .append("\n  TotalLength:    ").append(totalLength.toUShort())
            .append("\n  Checksum:       ").append(checksum.toUShort())
            .append("\n }")
        return sb.toString()
    }

    fun decode(data: ByteArray, offset: Int): NetUdpHeader {
        System.arraycopy(data, offset, rawData, 0, 8)
        return this
    }
}