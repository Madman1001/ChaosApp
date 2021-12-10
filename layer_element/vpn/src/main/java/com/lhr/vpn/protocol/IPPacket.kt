package com.lhr.vpn.protocol

import com.lhr.vpn.constant.PacketConstant
import java.lang.StringBuilder

/**
 * @author lhr
 * @date 2021/11/16
 * @des ip 数据报
 */
class IPPacket(private val bytes: ByteArray): IProtocol{
    protected @Volatile var mPacketRef: Long = 0L

    init {
        nativeInit(bytes)
    }

    protected fun finalize() {
        nativeRelease(mPacketRef)
        mPacketRef = 0
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

    fun getSourceAddress(): String {
        val hostname = getData(PacketConstant.DataOperateType.IP_SOURCE_ADDRESS.type) ?: 0
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

    fun getTargetAddress(): String {
        val hostname = getData(PacketConstant.DataOperateType.IP_TARGET_ADDRESS.type) ?: 0
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

    fun isTcp(): Boolean {
        return getUpperProtocol() == PacketConstant.DataType.TCP.type
    }

    fun isUdp(): Boolean {
        return getUpperProtocol() == PacketConstant.DataType.UDP.type
    }

    fun isICMP(): Boolean {
        return getUpperProtocol() == PacketConstant.DataType.ICMP.type
    }

    fun isIGMP(): Boolean {
        return getUpperProtocol() == PacketConstant.DataType.IGMP.type
    }

    override fun getRawData(): ByteArray {
        return bytes
    }

    private external fun nativeInit(bytes: ByteArray)
    private external fun nativeRelease(nativeRef: Long)
    private external fun nativeGetData(nativeRef: Long, dataType: Int): Any?
}