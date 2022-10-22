package com.lhr.vpn.socks.net.v4

import com.lhr.vpn.ext.hexToString
import java.nio.ByteBuffer

/**
 * @CreateDate: 2022/10/12
 * @Author: mac
 * @Description: udp数据包解析
 */
class NetUdpPacket {
    constructor()
    constructor(array: ByteArray): this(ByteBuffer.wrap(array))
    constructor(buffer: ByteBuffer){
        decodePacket(buffer)
    }

    //源端口号 16 bit
    var sourcePort: Short = 0

    //目标端口号 16 bit
    var targetPort: Short = 0

    //UDP长度(单位为：字节) 16 bit
    //var total_length: Short = 0

    //UDP校验和 16 bit
    var checksum: Short = 0
    //udp数据
    var data: ByteArray = ByteArray(0)

    fun decodePacket(buffer: ByteBuffer){
        sourcePort = buffer.short
        targetPort = buffer.short
        val totalLength = buffer.short
        checksum = buffer.short

        val dataByteLength = totalLength.toUShort().toInt() - 8
        val data = ByteArray(dataByteLength)
        buffer.get(data)
        this.data = data
    }

    /**
     * 返回数据包，无校验和
     */
    fun encodePacket(): ByteBuffer {
        val size = 8 + data.size
        return ByteBuffer.allocate(size)
            .putShort(sourcePort)
            .putShort(targetPort)
            .putShort(size.toShort())
            .putShort(0)
            .put(data)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("UDP Packet {")
            .append("\n  Src:            ").append(sourcePort.toUShort())
            .append("\n  Dst:            ").append(targetPort.toUShort())
            .append("\n  Data: [")
            .append(data.hexToString())
            .append("\n  ]")
        return sb.toString()
    }
}