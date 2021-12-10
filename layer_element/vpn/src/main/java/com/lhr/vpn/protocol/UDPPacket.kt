package com.lhr.vpn.protocol

import com.lhr.vpn.constant.PacketConstant
import java.lang.StringBuilder

/**
 * @author lhr
 * @date 2021/12/4
 * @des UDP数据类
 */
class UDPPacket: IProtocol{
    private lateinit var ipPacket: IPPacket

    constructor(bytes: ByteArray): this(IPPacket(bytes))

    constructor(ipPacket: IPPacket){
        this.ipPacket = ipPacket
    }

    fun getHostname(): String {
        return ipPacket.getTargetAddress()
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

    override fun getRawData(): ByteArray {
        return ipPacket.getRawData()
    }
}