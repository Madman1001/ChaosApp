package com.lhr.vpn.socks.net.v4

import android.util.Log
import com.lhr.vpn.ext.toHexString
import com.lhr.vpn.socks.net.IP_VERSION_4
import com.lhr.vpn.socks.net.PROTO_TCP
import com.lhr.vpn.socks.net.PROTO_UDP
import java.net.Inet4Address
import java.net.InetAddress
import java.nio.ByteBuffer

/**
 * @CreateDate: 2022/10/12
 * @Author: mac
 * @Description: ip数据包解析
 */
class NetIpPacket {
    constructor()
    constructor(array: ByteArray): this(ByteBuffer.wrap(array))
    constructor(buffer: ByteBuffer){
        decodePacket(buffer)
    }

    //版本号 4 bit
    var version: Int = IP_VERSION_4

    //头长度 4 bit (单位 32 bit)
    //var header_length: Int = 0

    //服务类型 8 bit
    var typeOfService: Byte = 0

    //总长度 16 bit (单位 8 bit)
    //var total_length: Short = 0

    //标识 16 bit
    var identification: Short = 0

    //标志 3 bit R　DF　MF
    var flag: Int = 0

    //片偏移 13 bit (记录分片在原报文中的相对位置，以8个字节为偏移单位)
    var offsetFrag: Int = 0

    //生存时间 8 bit
    var timeToLive: Byte = 0

    //上层协议 8 bit
    var upperProtocol: Byte = 0

    //头部校验和 16 bit
    var checksum: Short = 0

    //源ip地址 32 bit
    var sourceAddress: InetAddress = Inet4Address.getByAddress(ByteArray(4)) as Inet4Address

    //目标ip地址 32 bit
    var targetAddress: InetAddress = Inet4Address.getByAddress(ByteArray(4)) as Inet4Address

    //可选字段 (单位 32 bit)
    var optionWords: ByteArray = ByteArray(0)

    //ip数据
    var data: ByteArray = ByteArray(0)

    fun isTcp(): Boolean {
        return upperProtocol.toUByte().toInt() == PROTO_TCP
    }

    fun isUdp(): Boolean {
        return upperProtocol.toUByte().toInt() == PROTO_UDP
    }

    private fun decodePacket(rawData: ByteBuffer){
        val ipVersionAndLength = rawData.get().toUByte()

        val headerLength = (ipVersionAndLength.toInt() and 0x0f)

        version = (ipVersionAndLength.toInt() and 0xf0) shr 4

        typeOfService = rawData.get()

        val totalLength = rawData.short.toUShort().toInt()

        identification = rawData.short

        val ipFlagAndFragOffset = rawData.short
        flag = (ipFlagAndFragOffset.toUShort().toInt() and 0xE000) ushr 13
        offsetFrag = ipFlagAndFragOffset.toUShort().toInt() and 0x1FFF

        timeToLive = rawData.get()
        upperProtocol = rawData.get()

        checksum = rawData.short

        sourceAddress = readIpAddress(rawData)

        targetAddress = readIpAddress(rawData)

        val optionByteLength = (headerLength - 5) * 4
        val optionWords = ByteArray(optionByteLength)
        rawData.get(optionWords)
        this.optionWords = optionWords

        val dataByteLength = totalLength - headerLength * 4
        val data = ByteArray(dataByteLength)
        rawData.get(data)
        this.data = data
    }

    fun encodePacket(): ByteBuffer{
        val size = 20 + optionWords.size + data.size
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
            .put(((version shl 4) or (5 + optionWords.size / 4)).toByte())
            .put(typeOfService)
            .putShort(size.toShort())
            .putShort(identification)
            .putShort(((flag shl 13) or offsetFrag).toShort())
            .put(timeToLive)
            .put(upperProtocol)
            .putShort(0) //设置校验和
            .put(sourceAddress.address)
            .put(targetAddress.address)
            .put(optionWords)
            .put(data)

        //设置ip端校验和
        val ipChecksum = ChecksumUtil.checksum(buffer.array(), 20 + optionWords.size)
        buffer.position(10)
        buffer.putShort(ipChecksum.toShort())
        buffer.rewind()
        return buffer
    }

    private fun tcpUdpChecksum(data: ByteArray): Int {
        val buffer = ByteBuffer.wrap(ByteArray(12 + data.size))
        buffer.put(sourceAddress.address)
        buffer.put(targetAddress.address)
        buffer.put(0)
        buffer.put(upperProtocol)
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
        return ChecksumUtil.checksum(checkData, checkData.size)
    }

    private fun readIpAddress(packet: ByteBuffer): Inet4Address {
        val ipAddr = ByteArray(4)
        packet[ipAddr]
        return Inet4Address.getByAddress(ipAddr) as Inet4Address
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("IPv4 Packet {")
            .append("\n  Version:        ").append(version)
            .append("\n  Header length:  ").append(20 + optionWords.size)
            .append("\n  Type:           ").append(typeOfService)
            .append("\n  Total length:   ").append(20 + optionWords.size + data.size)
            .append("\n  Identification: ").append(identification)
            .append("\n  Flags + offset: ").append(((flag shl 13) or offsetFrag).toShort())
            .append("\n  Time to live:   ").append(timeToLive)
            .append("\n  Protocol:       ").append(upperProtocol)
            .append("\n  Source:         ").append(sourceAddress.hostAddress)
            .append("\n  Destination:    ").append(targetAddress.hostAddress)
            .append("\n  Options: [")
            .append(optionWords.toHexString())
            .append("\n  ]")
            .append("\n  Data: [")
            .append(data.toHexString())
            .append("\n  ]")
        return sb.toString()
    }
}