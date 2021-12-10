package com.lhr.vpn.protocol

import android.util.Log
import com.lhr.vpn.constant.PacketConstant

/**
 * @author lhr
 * @date 2021/11/16
 * @des ip 数据报
 */
open class IPPacket : IProtocol {
    protected @Volatile
    var mPacketRef: Long = 0L

    private external fun nativeInit()
    private external fun nativeSetRawData(nativeRef: Long, bytes: ByteArray)
    private external fun nativeGetRawData(nativeRef: Long): ByteArray
    private external fun nativeRelease(nativeRef: Long)
    private external fun nativeGetAttribute(nativeRef: Long, dataType: Int): Any?
    private external fun nativeSetAttribute(nativeRef: Long, dataType: Int, data: Any)

    init {
        nativeInit()
    }

    constructor()

    constructor(byteArray: ByteArray){
        setRawData(byteArray)
    }

    protected fun finalize() {
        nativeRelease(mPacketRef)
        mPacketRef = 0
    }

    fun isValid(): Boolean {
        return if (mPacketRef != 0L) {
            getIpVersion() != 0
        } else {
            false
        }
    }

    fun getAttribute(dataType: PacketConstant.DataOperateType): Any? {
        return nativeGetAttribute(mPacketRef, dataType.value)
    }

    fun setAttribute(dataType: PacketConstant.DataOperateType, data: Any) {
        nativeSetAttribute(mPacketRef, dataType.value, data)
    }

    fun getIpVersion(): Int {
        val version = getAttribute(PacketConstant.DataOperateType.IP_VERSION) ?: 0
        return version as Int
    }

    fun getUpperProtocol(): Int {
        val protocol = getAttribute(PacketConstant.DataOperateType.IP_UPPER_PROTOCOL) ?: 0
        return protocol as Int
    }

    fun setUpperProtocol(upperProtocol: Int) {
        setAttribute(PacketConstant.DataOperateType.IP_UPPER_PROTOCOL, upperProtocol)
    }

    fun getSourceAddress(): String {
        val hostname = getAttribute(PacketConstant.DataOperateType.IP_SOURCE_ADDRESS) ?: 0
        val sb = StringBuilder()
        if (hostname != 0) {
            var address = hostname as Int
            for (i in 0 until 4) {
                sb.insert(0, address and 0xFF)
                if (i != 3) {
                    sb.insert(0, '.')
                }
                address = address ushr 8
            }
        }
        return sb.toString()
    }

    fun setSourceAddress(address: String) {
        if (address.contains(".")) {
            val addressList = address.split(".")
            if (addressList.size == 4) {
                var addr = 0x00000000
                for (subAddr in addressList) {
                    addr = addr shl 8
                    addr = addr or subAddr.toInt()
                }
                setAttribute(PacketConstant.DataOperateType.IP_SOURCE_ADDRESS, addr)
            } else {
                throw RuntimeException("Set Source Address Exception, address:$address")
            }
        } else {
            throw RuntimeException("Set Source Address Exception, address:$address")
        }
    }

    fun getTargetAddress(): String {
        val hostname = getAttribute(PacketConstant.DataOperateType.IP_TARGET_ADDRESS) ?: 0
        val sb = StringBuilder()
        if (hostname != 0) {
            var address = hostname as Int
            for (i in 0 until 4) {
                sb.insert(0, address and 0xFF)
                if (i != 3) {
                    sb.insert(0, '.')
                }
                address = address ushr 8
            }
        }
        return sb.toString()
    }

    fun setTargetAddress(address: String) {
        if (address.contains(".")) {
            val addressList = address.split(".")
            if (addressList.size == 4) {
                var addr = 0x00000000
                for (subAddr in addressList) {
                    addr = addr shl 8
                    addr = addr or subAddr.toInt()
                }
                setAttribute(PacketConstant.DataOperateType.IP_TARGET_ADDRESS, addr)
            } else {
                throw RuntimeException("Set Target Address Exception, address:$address")
            }
        } else {
            throw RuntimeException("Set Target Address Exception, address:$address")
        }
    }

    fun getTimeToLive(): Int{
        val ttl = getAttribute(PacketConstant.DataOperateType.IP_TIME_TO_LIVE) ?: 0
        return ttl as Int
    }

    fun setTimeToLive(ttl: Int){
        setAttribute(PacketConstant.DataOperateType.IP_TIME_TO_LIVE, ttl)
    }

    fun getFlag(): Byte {
        val flag = getAttribute(PacketConstant.DataOperateType.IP_FLAG) ?: 0
        return flag as Byte
    }

    fun setFlag(flag: Byte){
        setAttribute(PacketConstant.DataOperateType.IP_FLAG, flag)
    }

    fun getIdentification(): Short{
        val identification = getAttribute(PacketConstant.DataOperateType.IP_IDENTIFICATION) ?: 0
        return identification as Short
    }

    fun setIdentification(identification: Short){
       setAttribute(PacketConstant.DataOperateType.IP_IDENTIFICATION, identification)
    }

    fun getOffsetFrag(): Int{
        val offsetFrag = getAttribute(PacketConstant.DataOperateType.IP_OFFSET_FRAG) ?: 0
        return offsetFrag as Int
    }

    fun setOffsetFrag(offsetFrag: Int) {
        setAttribute(PacketConstant.DataOperateType.IP_OFFSET_FRAG, offsetFrag)
    }

    fun isTcp(): Boolean {
        return getUpperProtocol() == PacketConstant.DataType.TCP.value
    }

    fun isUdp(): Boolean {
        return getUpperProtocol() == PacketConstant.DataType.UDP.value
    }

    fun isICMP(): Boolean {
        return getUpperProtocol() == PacketConstant.DataType.ICMP.value
    }

    fun isIGMP(): Boolean {
        return getUpperProtocol() == PacketConstant.DataType.IGMP.value
    }

    override fun getRawData(): ByteArray {
        return if (mPacketRef != 0L) {
            try {
                nativeGetRawData(mPacketRef)
            } catch (e: Exception) {
                ByteArray(0)
            }
        } else {
            ByteArray(0)
        }
    }

    override fun setRawData(byteArray: ByteArray) {
        if (mPacketRef != 0L) {
            nativeSetRawData(mPacketRef, byteArray)
        }
    }
}