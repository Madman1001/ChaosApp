package com.lhr.vpn.socks.net.v4

import com.lhr.vpn.ext.hexToString
import java.nio.ByteBuffer

/**
 * @CreateDate: 2022/10/12
 * @Author: mac
 * @Description: tcp数据包解析
 */
//(0 0 URG ACK PSH RST SYN FIN)
const val SIGN_NUL = 0x00
const val SIGN_URG = 0x20
const val SIGN_ACK = 0x10
const val SIGN_PSH = 0x08
const val SIGN_RST = 0x04
const val SIGN_SYN = 0x02
const val SIGN_FIN = 0x01
class NetTcpPacket {
    constructor()
    constructor(array: ByteArray) : this(ByteBuffer.wrap(array))
    constructor(buffer: ByteBuffer) {
        decodePacket(buffer)
    }

    //源端口号 16 bit
    var sourcePort: Short = 0

    //目标端口号 16 bit
    var targetPort: Short = 0

    //序号 32 bit
    var sequenceNumber: Int = 0

    //确认序号 32 bit
    var ackSequenceNumber: Int = 0

    // 首部长度 4 bit (以 32 bit为单位)
    // var head_length: Byte = 0

    //控制位 6 bit (0 0 URG ACK PSH RST SYN FIN)
    var controlSign: Int = 0

    //窗口大小 16 bit
    var windowSize: Short = 0

    //校验和 16 bit
    var checksum: Short = 0

    //紧急指针 16 bit
    var urgentPointer: Short = 0

    //其它选项 (单位 32 bit)
    var optionWords: ByteArray = ByteArray(0)

    //tcp数据
    var data: ByteArray = ByteArray(0)

    fun decodePacket(buffer: ByteBuffer) {
        sourcePort = buffer.short
        targetPort = buffer.short
        sequenceNumber = buffer.int
        ackSequenceNumber = buffer.int

        val len = buffer.get().toUByte().toInt()
        val headLength = len ushr 4

        controlSign = buffer.get().toUByte().toInt()

        windowSize = buffer.short

        checksum = buffer.short

        urgentPointer = buffer.short

        val optionsLength = headLength * 4 - 20
        val optionWords = ByteArray(optionsLength)
        buffer.get(optionWords)
        this.optionWords = optionWords

        val dataLength = buffer.capacity() - buffer.position()
        val data = ByteArray(dataLength)
        buffer.get(data)
        this.data = data
    }

    /**
     * 返回数据包，无校验和
     */
    fun encodePacket(): ByteBuffer {
        val size = 20 + optionWords.size + data.size

        return ByteBuffer.allocate(size)
            .putShort(sourcePort)
            .putShort(targetPort)
            .putInt(sequenceNumber)
            .putInt(ackSequenceNumber)
            .put((((20 + optionWords.size) / 4) shl 4).toByte())
            .put(controlSign.toByte())
            .putShort(windowSize)
            .putShort(0)
            .putShort(urgentPointer)
            .put(optionWords)
            .put(data)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("TCP Packet {")
            .append("\n  Src:            ").append(sourcePort.toUShort())
            .append("\n  Dst:            ").append(targetPort.toUShort())
            .append("\n  SeqNumber:      ").append(sequenceNumber.toUInt())
            .append("\n  AckNumber:      ").append(ackSequenceNumber.toUInt())
            .append("\n  WindowSize:     ").append(windowSize.toUShort())
            .append("\n  ControlSign     ")
            .append(if (controlSign and SIGN_URG != 0) "URG " else "")
            .append(if (controlSign and SIGN_ACK != 0) "ACK " else "")
            .append(if (controlSign and SIGN_PSH != 0) "PSH " else "")
            .append(if (controlSign and SIGN_RST != 0) "RST " else "")
            .append(if (controlSign and SIGN_SYN != 0) "SYN " else "")
            .append(if (controlSign and SIGN_FIN != 0) "FIN " else "")
            .append("\n  Options: [")
            .append(optionWords.hexToString())
            .append("\n  ]")
            .append("\n  Data: [")
            .append(data.hexToString())
            .append("\n  ]")
        return sb.toString()
    }
}