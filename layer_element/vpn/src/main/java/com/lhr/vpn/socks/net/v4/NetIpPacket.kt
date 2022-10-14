package com.lhr.vpn.socks.net.v4

import com.lhr.vpn.util.ByteLog
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
        return ipHeader.upper_protocol.toUByte().toInt() == NetV4Protocol.PROTO_TCP
    }

    fun isUdp(): Boolean {
        return ipHeader.upper_protocol.toUByte().toInt() == NetV4Protocol.PROTO_UDP
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
        ipHeader.flag = (ipFlagAndFragOffset.toUShort().toInt() and 0xE000) ushr 13
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
        val upperChecksum = tcpUdpChecksum(data).toShort()
        if (isTcp()){
            data = ByteBuffer.wrap(data).run {
                position(16)
                putShort(upperChecksum)
            }.array()
        } else if (isUdp()){
            data = ByteBuffer.wrap(data).run {
                position(6)
                putShort(upperChecksum)
            }.array()
        }

        val buffer = ByteBuffer.allocate(size)
            .put(((ipHeader.version shl 4) or (5 + ipHeader.optionWords.size / 4)).toByte())
            .put(ipHeader.type_of_service)
            .putShort(size.toShort())
            .putShort(ipHeader.identification)
            .putShort(((ipHeader.flag shl 13) or ipHeader.offset_frag).toShort())
            .put(ipHeader.time_to_live)
            .put(ipHeader.upper_protocol)
            .putShort(0) //设置校验和
            .put(ipHeader.source_ip_address.address)
            .put(ipHeader.target_ip_address.address)
            .put(ipHeader.optionWords)
            .put(data)

        //设置ip端校验和
        val ipChecksum = NetV4Protocol.checksum(buffer.array(), 20 + ipHeader.optionWords.size)
        buffer.position(10)
        buffer.putShort(ipChecksum.toShort())
        buffer.rewind()
        return buffer
    }

    private fun tcpUdpChecksum(data: ByteArray): Int {
        val buffer = ByteBuffer.wrap(ByteArray(12 + data.size))
        buffer.put(ipHeader.source_ip_address.address)
        buffer.put(ipHeader.target_ip_address.address)
        buffer.put(0)
        buffer.put(ipHeader.upper_protocol)
        buffer.putShort(data.size.toShort())
        buffer.put(data)
        if (isTcp()) {
            buffer.position(12 + 16)
            buffer.putShort(0)
        }
        if (isUdp()) {
            buffer.position(12 + 6)
            buffer.putShort(0)
        }
        buffer.rewind()
        val checkData = buffer.array()
        return NetV4Protocol.checksum(checkData, checkData.size)
    }

    private fun readIpAddress(packet: ByteBuffer): Inet4Address {
        val ipAddr = ByteArray(4)
        packet[ipAddr]
        return Inet4Address.getByAddress(ipAddr) as Inet4Address
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("IPv4 Packet {")
            .append("\n  Version:        ").append(ipHeader.version)
            .append("\n  Header length:  ").append(20 + ipHeader.optionWords.size)
            .append("\n  Type:           ").append(ipHeader.type_of_service)
            .append("\n  Total length:   ").append(20 + ipHeader.optionWords.size + data.size)
            .append("\n  Identification: ").append(ipHeader.identification)
            .append("\n  Flags + offset: ").append(((ipHeader.flag shl 13) or ipHeader.offset_frag).toShort())
            .append("\n  Time to live:   ").append(ipHeader.time_to_live)
            .append("\n  Protocol:       ").append(ipHeader.upper_protocol)
            .append("\n  Source:         ").append(ipHeader.source_ip_address.hostAddress)
            .append("\n  Destination:    ").append(ipHeader.target_ip_address.hostAddress)
            .append("\n  Options: [")
            .append(ByteLog.hexToString(ipHeader.optionWords))
            .append("\n  ]")
            .append("\n  Data: [")
            .append(ByteLog.hexToString(data))
            .append("\n  ]")
        return sb.toString()
    }

    class IpHeader{
        //版本号 4 bit
        var version: Int = NetV4Protocol.IP_VERSION

        //头长度 4 bit (单位 32 bit)
        //var header_length: Int = 0

        //服务类型 8 bit
        var type_of_service: Byte = 0

        //总长度 16 bit (单位 8 bit)
        //var total_length: Short = 0

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
}