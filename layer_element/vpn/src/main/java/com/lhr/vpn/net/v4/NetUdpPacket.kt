package com.lhr.vpn.net.v4

import java.nio.ByteBuffer

/**
 * @CreateDate: 2022/10/12
 * @Author: mac
 * @Description: udp数据包解析
 */
class NetUdpPacket {
    constructor(){
        udpHeader = UdpHeader()
    }
    constructor(array: ByteArray): this(ByteBuffer.wrap(array))

    constructor(buffer: ByteBuffer){
        decodePacket(buffer)
    }

    //udp协议头
    lateinit var udpHeader: UdpHeader
        private set

    //udp数据
    var data: ByteArray = ByteArray(0)

    fun decodePacket(buffer: ByteBuffer){
        val udpHeader = UdpHeader()
        udpHeader.source_port = buffer.short
        udpHeader.target_port = buffer.short
        val totalLength = buffer.short
        udpHeader.check_sum = buffer.short
        this.udpHeader = udpHeader

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
            .putShort(udpHeader.source_port)
            .putShort(udpHeader.target_port)
            .putShort(size.toShort())
            .putShort(0)
            .put(data)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("udp packet:\n")
            .append("Src:").append(udpHeader.source_port.toUShort()).append("\n")
            .append("Dst:").append(udpHeader.target_port.toUShort()).append("\n")
            .append("DataLength:").append(data.size).append("\n")
        return sb.toString()
    }

    class UdpHeader {
        //源端口号 16 bit
        var source_port: Short = 0

        //目标端口号 16 bit
        var target_port: Short = 0

        //UDP长度(单位为：字节) 16 bit
        //var total_length: Short = 0

        //UDP校验和 16 bit
        var check_sum: Short = 0
    }
}