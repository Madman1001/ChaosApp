package com.lhr.vpn.socks.net.v4

/**
 * @author lhr
 * @date 2021/12/8
 * @des 协议接口
 */
object NetV4Protocol{
    const val IP_VERSION = 4

    const val PROTO_TCP = 6
    const val PROTO_UDP = 17
    const val PROTO_ICMP = 1
    const val PROTO_IGMP = 2

    const val MAX_PACKET_SIZE = 0xFF
    const val MAX_IP_PACKET_HEADER_SIZE = 0xF
    /**
     * 计算校验和
     */
    fun checksum(data: ByteArray, len: Int): Int{
        var sum = 0
        for (i in 0 until len step 2){
            sum += (data[i].toInt() and 0xFF) shl 8
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        for (i in 1 until len step 2){
            sum += (data[i].toInt() and 0xFF)
            sum = (sum and 0xFFFF) + (sum shr 16)
        }

        sum = (sum and 0xFFFF) + (sum shr 16)
        return sum.inv()
    }
}