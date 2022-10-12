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
        udpHeader.total_length = buffer.short
        udpHeader.check_sum = buffer.short
        this.udpHeader = udpHeader

        val dataByteLength = udpHeader.total_length.toUShort().toInt() - 8
        val data = ByteArray(dataByteLength)
        buffer.get(data)
        this.data = data
    }

    fun encodePacket(): ByteBuffer {
        val size = 8 + data.size
        val buffer = ByteBuffer.allocate(size)
        buffer.putShort(udpHeader.source_port)
        buffer.putShort(udpHeader.target_port)
        //todo 需计算长度
        buffer.putShort(udpHeader.total_length)
        //todo 需计算校验和
        buffer.putShort(udpHeader.check_sum)
        buffer.put(data)
        return buffer
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
        var total_length: Short = 0

        //UDP校验和 16 bit
        var check_sum: Short = 0
    }
}