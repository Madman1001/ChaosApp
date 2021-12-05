package com.lhr.vpn.protocol

import java.lang.StringBuilder

/**
 * @author lhr
 * @date 2021/12/4
 * @des UDP数据类
 */
class UDPPacket(private val ipPacket: IPPacket) {
    fun getHostname(): String{
        val hostname = ipPacket.getData(PacketConstant.DataOperateType.IP_TARGET_ADDRESS.type) ?: 0
        val sb = StringBuilder()
        if (hostname != 0){
            var address = hostname as Int
            for (i in 0 until 4) {
                sb.insert(0,address and 0xFF)
                if (i != 3){
                    sb.insert(0,'.')
                }
                address = address ushr 8
            }
        }
        return sb.toString()
    }

    fun getSourcePort(): Int{
        val sourcePort = ipPacket.getData(PacketConstant.DataOperateType.UDP_SOURCE_PORT.type) ?: 0
        return sourcePort as Int
    }

    fun getTargetPort(): Int{
        val targetPort = ipPacket.getData(PacketConstant.DataOperateType.UDP_TARGET_PORT.type) ?: 0
        return targetPort as Int
    }

    fun getData(): ByteArray{
        val data = ipPacket.getData(PacketConstant.DataOperateType.UDP_DATA.type) ?: ByteArray(0)
        return data as ByteArray
    }
}