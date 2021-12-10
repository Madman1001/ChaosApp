package com.lhr.vpn.protocol

import com.lhr.vpn.constant.PacketConstant

/**
 * @author lhr
 * @date 2021/12/4
 * @des UDP数据类
 */
class UDPPacket : IPPacket {
    constructor() {
        super.setUpperProtocol(PacketConstant.DataType.UDP.value)
    }

    constructor(bytes: ByteArray): super(bytes)

    constructor(ipPacket: IPPacket) {
        super.setRawData(ipPacket.getRawData())
    }

    fun getSourcePort(): Int {
        val sourcePort = super.getAttribute(PacketConstant.DataOperateType.UDP_SOURCE_PORT) ?: 0
        return sourcePort as Int
    }

    fun setSourcePort(port: Int){
        super.setAttribute(PacketConstant.DataOperateType.UDP_SOURCE_PORT, port)
    }

    fun getTargetPort(): Int {
        val targetPort = super.getAttribute(PacketConstant.DataOperateType.UDP_TARGET_PORT) ?: 0
        return targetPort as Int
    }

    fun setTargetPort(port: Int){
        super.setAttribute(PacketConstant.DataOperateType.UDP_TARGET_PORT, port)
    }

    fun getData(): ByteArray {
        val data = super.getAttribute(PacketConstant.DataOperateType.UDP_DATA) ?: ByteArray(0)
        return data as ByteArray
    }

    fun setData(data: ByteArray, offset: Int = -1, length: Int = -1) {
        val dataOffset = if (offset == -1){
            0
        }else{
            offset
        }

        val dataLength = if (length == -1){
            data.size
        }else{
            length
        }

        super.setAttribute(PacketConstant.DataOperateType.UDP_DATA, data.copyOfRange(dataOffset, dataOffset + dataLength))
    }
}