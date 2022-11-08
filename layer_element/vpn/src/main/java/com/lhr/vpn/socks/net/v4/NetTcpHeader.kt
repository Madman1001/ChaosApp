package com.lhr.vpn.socks.net.v4

import com.lhr.vpn.*
import com.lhr.vpn.socks.net.*

/**
 * @CreateDate: 2022/10/12
 * @Author: mac
 * @Description: tcp数据包解析
 */
class NetTcpHeader {
    val rawData: ByteArray = ByteArray(20)
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

    //序号 32 bit ----- [4-7]
    var seqNumber: Int
        get() = rawData.getInt(offset + 4)
        set(value) {
            rawData.setInt(offset + 4, value)
        }

    //确认序号 32 bit ----- [8-11]
    var ackNumber: Int
        get() = rawData.getInt(offset + 8)
        set(value) {
            rawData.setInt(offset + 8, value)
        }

    // 首部长度 4 bit (以 32 bit为单位) ----- [12]
    var headerLength: Int
        get() = ((rawData[offset + 12].toNetInt()) and 0xf0) ushr 4
        set(value) {
            rawData[offset + 12] = ((value shl 4 and 0xf0) or rawData[offset + 12].toNetInt()).toByte()
        }

    //控制位 6 bit (0 0 URG ACK PSH RST SYN FIN) ----- [13]
    var controlSign: Int
        get() = rawData[offset + 13].toInt()
        set(value) {
            rawData[offset + 13] = value.toByte()
        }

    //窗口大小 16 bit ----- [14-15]
    var windowSize: Short
        get() = rawData.getShort(offset + 14)
        set(value) {
            rawData.setShort(offset + 14, value)
        }

    //校验和 16 bit ----- [16-17]
    var checksum: Short
        get() = rawData.getShort(offset + 16)
        set(value) {
            rawData.setShort(offset + 16, value)
        }

    //紧急指针 16 bit ----- [18-19]
    var urgentPointer: Short
        get() = rawData.getShort(offset + 18)
        set(value) {
            rawData.setShort(offset + 18, value)
        }

    //其它选项 (单位 32 bit)
    var optionData: ByteArray = ByteArray(0)

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("TCP Packet {")
            .append("\n  Src:            ").append(sourcePort.toUShort())
            .append("\n  Dst:            ").append(destinationPort.toUShort())
            .append("\n  SeqNumber:      ").append(seqNumber.toUInt())
            .append("\n  AckNumber:      ").append(ackNumber.toUInt())
            .append("\n  HeaderLen:      ").append(headerLength.toUInt())
            .append("\n  ControlSign     ")
            .append(if (controlSign and SIGN_URG != 0) "URG " else "")
            .append(if (controlSign and SIGN_ACK != 0) "ACK " else "")
            .append(if (controlSign and SIGN_PSH != 0) "PSH " else "")
            .append(if (controlSign and SIGN_RST != 0) "RST " else "")
            .append(if (controlSign and SIGN_SYN != 0) "SYN " else "")
            .append(if (controlSign and SIGN_FIN != 0) "FIN " else "")
            .append("\n  WindowSize:     ").append(windowSize.toUShort())
            .append("\n  Checksum:       ").append(checksum.toUShort())
            .append("\n  UrgentPointer:  ").append(urgentPointer.toUShort())
            .append("\n  Options: [")
            .append(optionData.toHexString() + "\n")
            .append("   ]")
            .append("\n }")
        return sb.toString()
    }

    fun decode(data: ByteArray, offset: Int = 0): NetTcpHeader {
        return this.apply {
            System.arraycopy(data, offset, rawData, 0, 20)
            val optionByteLength = headerLength * 4 - 20
            val tempData = ByteArray(optionByteLength)
            System.arraycopy(data, offset + 20, tempData, 0, optionByteLength)
            optionData = tempData
        }
    }
}