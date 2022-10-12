package com.lhr.vpn.net.v4

import java.net.Inet4Address
import java.nio.ByteBuffer

/**
 * @CreateDate: 2022/10/12
 * @Author: mac
 * @Description: ip数据包解析
 */
class NetIpPacket {
    constructor(){
        ipHeader = IpHeader()
    }
    constructor(array: ByteArray): this(ByteBuffer.wrap(array))

    constructor(buffer: ByteBuffer){
        decodePacket(buffer)
    }

    //ip协议头
    lateinit var ipHeader: IpHeader
        private set

    //ip数据
    var data: ByteArray = ByteArray(0)

    fun isTcp(): Boolean {
        return ipHeader.upper_protocol.toUByte().toInt() == PROTO_TCP
    }

    fun isUdp(): Boolean {
        return ipHeader.upper_protocol.toUByte().toInt() == PROTO_UDP
    }

    private fun decodePacket(rawData: ByteBuffer){
        val ipHeader = IpHeader()

        val ipVersionAndLength = rawData.get().toUByte()

        val headerLength = (ipVersionAndLength.toInt() and 0x0f)

        ipHeader.version = (ipVersionAndLength.toInt() and 0xf0) shr 4

        ipHeader.type_of_service = rawData.get()

        val totalLength = rawData.short.toUShort().toInt()

        ipHeader.identification = rawData.short

        val ipFlagAndFragOffset = rawData.short
        ipHeader.flag = ipFlagAndFragOffset.toUShort().toInt() and 0xE000
        ipHeader.offset_frag = ipFlagAndFragOffset.toUShort().toInt() and 0x1FFF

        ipHeader.time_to_live = rawData.get()
        ipHeader.upper_protocol = rawData.get()

        ipHeader.head_check_sum = rawData.short

        ipHeader.source_ip_address = readIpAddress(rawData)

        ipHeader.target_ip_address = readIpAddress(rawData)

        val optionByteLength = (headerLength - 5) * 4
        val optionWords = ByteArray(optionByteLength)
        rawData.get(optionWords)
        ipHeader.optionWords = optionWords

        this.ipHeader = ipHeader

        val dataByteLength = totalLength - headerLength * 4
        val data = ByteArray(dataByteLength)
        rawData.get(data)
        this.data = data
    }

    fun encodePacket(): ByteBuffer{
        val size = 20 + ipHeader.optionWords.size + data.size
        val buffer = ByteBuffer.allocate(size)


        return buffer
    }

    private fun readIpAddress(packet: ByteBuffer): Inet4Address {
        val ipAddr = ByteArray(4)
        packet[ipAddr]
        return Inet4Address.getByAddress(ipAddr) as Inet4Address
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("ip packet:\n")
            .append("Ver:").append(ipHeader.version).append("\n")
            .append("Proto:").append(ipHeader.upper_protocol).append("\n")
            .append("Src:").append(ipHeader.source_ip_address.toString()).append("\n")
            .append("Dst:").append(ipHeader.target_ip_address.toString()).append("\n")
            .append("DataLength:").append(data.size).append("\n")

        return sb.toString()
    }

    class IpHeader{
        //版本号 4 bit
        var version: Int = 0

        //头长度 4 bit (单位 32 bit)
        var header_length: Int = 0

        //服务类型 8 bit
        var type_of_service: Byte = 0

        //总长度 16 bit (单位 8 bit)
        var total_length: Short = 0

        //标识 16 bit
        var identification: Short = 0

        //标志 3 bit
        var flag: Int = 0

        //片偏移 13 bit (记录分片在原报文中的相对位置，以8个字节为偏移单位)
        var offset_frag: Int = 0

        //生存时间 8 bit
        var time_to_live: Byte = 0

        //上层协议 8 bit
        var upper_protocol: Byte = 0

        //头部校验和 16 bit
        var head_check_sum: Short = 0

        //源ip地址 32 bit
        var source_ip_address: Inet4Address = Inet4Address.getByAddress(ByteArray(4)) as Inet4Address

        //目标ip地址 32 bit
        var target_ip_address: Inet4Address = Inet4Address.getByAddress(ByteArray(4)) as Inet4Address

        //可选字段 (单位 32 bit)
        var optionWords: ByteArray = ByteArray(0)
    }

    companion object{
        const val PROTO_TCP = 6
        const val PROTO_UDP = 17
        const val PROTO_ICMP = 1
        const val PROTO_IGMP = 2
    }
}