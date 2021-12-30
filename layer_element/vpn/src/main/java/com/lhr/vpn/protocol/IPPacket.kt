package com.lhr.vpn.protocol

import com.lhr.vpn.constant.PacketConstant

/**
 * @author lhr
 * @date 2021/11/16
 * @des ip 数据报
 */
open class IPPacket : IProtocol {
    protected val tag = this.javaClass.simpleName

    @Volatile
    var mPacketRef: Long = 0L
        protected set

    private external fun nativeInit()
    private external fun nativeSetRawData(nativeRef: Long, bytes: ByteArray)
    private external fun nativeGetRawData(nativeRef: Long): ByteArray?
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

    open fun isValid(): Boolean {
        return if (mPacketRef != 0L) {
            //暂时只支持IPv4
            getIpVersion() == 4
        } else {
            false
        }
    }

    open fun getAttribute(dataType: PacketConstant.DataOperateType): Any? {
        return nativeGetAttribute(mPacketRef, dataType.value)
    }

    open fun setAttribute(dataType: PacketConstant.DataOperateType, data: Any) {
        nativeSetAttribute(mPacketRef, dataType.value, data)
    }

    open fun getIpVersion(): Int {
        val version = getAttribute(PacketConstant.DataOperateType.IP_VERSION) ?: 0
        return version as Int
    }

    open fun getUpperProtocol(): Int {
        val protocol = getAttribute(PacketConstant.DataOperateType.IP_UPPER_PROTOCOL) ?: 0
        return protocol as Int
    }

    open fun setUpperProtocol(upperProtocol: Int) {
        setAttribute(PacketConstant.DataOperateType.IP_UPPER_PROTOCOL, upperProtocol)
    }

    open fun getSourceAddress(): String {
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

    open fun setSourceAddress(address: String) {
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

    open fun getTargetAddress(): String {
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

    open fun setTargetAddress(address: String) {
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

    open fun getSourcePort(): Int {
        throw RuntimeException("Method not implemented Exception:getSourcePort")
    }

    open fun setSourcePort(port: Int){
        throw RuntimeException("Method not implemented Exception:setSourcePort")
    }

    open fun getTargetPort(): Int {
        throw RuntimeException("Method not implemented Exception:getTargetPort")
    }

    open fun setTargetPort(port: Int){
        throw RuntimeException("Method not implemented Exception:setTargetPort")
    }

    open fun getData(): ByteArray {
        throw RuntimeException("Method not implemented Exception:getData")
    }

    open fun setData(data: ByteArray, offset: Int, length: Int) {
        throw RuntimeException("Method not implemented Exception:setData")
    }

    open fun getTimeToLive(): Int {
        val ttl = getAttribute(PacketConstant.DataOperateType.IP_TIME_TO_LIVE) ?: 0
        return ttl as Int
    }

    open fun setTimeToLive(ttl: Int){
        setAttribute(PacketConstant.DataOperateType.IP_TIME_TO_LIVE, ttl)
    }

    open fun getFlag(): Byte {
        val flag = getAttribute(PacketConstant.DataOperateType.IP_FLAG) ?: 0
        return flag as Byte
    }

    open fun setFlag(flag: Byte){
        setAttribute(PacketConstant.DataOperateType.IP_FLAG, flag)
    }

    open fun getIdentification(): Short{
        val identification = getAttribute(PacketConstant.DataOperateType.IP_IDENTIFICATION) ?: 0
        return identification as Short
    }

    open fun setIdentification(identification: Short){
       setAttribute(PacketConstant.DataOperateType.IP_IDENTIFICATION, identification)
    }

    open fun getOffsetFrag(): Int{
        val offsetFrag = getAttribute(PacketConstant.DataOperateType.IP_OFFSET_FRAG) ?: 0
        return offsetFrag as Int
    }

    open fun setOffsetFrag(offsetFrag: Int) {
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

    final override fun getRawData(): ByteArray {
        return if (mPacketRef != 0L) {
            try {
                nativeGetRawData(mPacketRef) ?: ByteArray(0)
            } catch (e: Exception) {
                ByteArray(0)
            }
        } else {
            ByteArray(0)
        }
    }

    final override fun setRawData(byteArray: ByteArray) {
        if (mPacketRef != 0L) {
            nativeSetRawData(mPacketRef, byteArray)
        }
    }
}