package com.lhr.vpn.protocol

/**
 * @author lhr
 * @date 2021/11/16
 * @des ip 数据报
 */
class IPPacket(private val bytes: ByteArray) {
    protected var mPacketRef: Int = 0

    init {
        nativeInit(bytes)
    }

    protected fun finalize() {
//        nativeRelease(mPacketRef)
    }

    fun getData(dataType: Int): Any?{
        return nativeGetData(mPacketRef,dataType)
    }

    fun getIpVersion(): Int {
        val version = getData(PacketConstant.DataOperateType.IP_VERSION.type) ?: 0
        return version as Int
    }

    fun getUpperProtocol(): Int {
        val protocol = getData(PacketConstant.DataOperateType.IP_UPPER_PROTOCOL.type) ?: 0
        return protocol as Int
    }

    fun isTcp(): Boolean {
        return getUpperProtocol() == PacketConstant.DataType.TCP.type
    }

    fun isUdp(): Boolean {
        return getUpperProtocol() == PacketConstant.DataType.UDP.type
    }

    private external fun nativeInit(bytes: ByteArray)
    private external fun nativeRelease(nativeRef: Int)
    private external fun nativeGetData(nativeRef: Int, dataType: Int): Any?

}