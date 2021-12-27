package com.lhr.vpn.protocol

import android.util.Log
import com.lhr.vpn.constant.PacketConstant
import java.lang.RuntimeException
import java.lang.StringBuilder
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * @author lhr
 * @date 2021/12/4
 * @des UDP数据类
 */
class TCPPacket : IPPacket {

    enum class ControlFlag(val value: Byte) {
        FIN(0x01),
        SYN(0x02),
        RST(0x04),
        PSH(0x08),
        ACK(0x10),
        URG(0x20)
    }

    enum class Option(val kind: Byte, val len: Byte){
        EOL(0,1), //End of Option List 结束TCP选项列表
        NOP(1,1),//No Operation 空操作，用于填充TCP选项
        MSS(2,4),//最大报文段 Max Segment Size
        WSOPT(3,3),// Window Scaling Factor，只能在SYN包中发送
        SACK_P(4,2),//发送方支持SACK选项
        SACK(5,-1),//-1表示长度不确定，SACK块信息
        TSOPT(8,10),//Timestamps 选项
        FOC(34,-1),//-1表示长度不确定，TFO中传递cookie
    }

    constructor() {
        super.setUpperProtocol(PacketConstant.DataType.TCP.value)
    }

    constructor(bytes: ByteArray){
        super.setRawData(bytes)
        if (!super.isTcp()){
            throw RuntimeException("The ip packet upper protocol is no TCP")
        }
    }

    constructor(ipPacket: IPPacket){
        if (!ipPacket.isTcp()){
            throw RuntimeException("The ip packet upper protocol is no TCP")
        }
        super.setRawData(ipPacket.getRawData())
    }

    fun getSourcePort(): Int {
        val sourcePort = super.getAttribute(PacketConstant.DataOperateType.TCP_SOURCE_PORT) ?: 0
        return sourcePort as Int
    }

    fun setSourcePort(port: Int){
        super.setAttribute(PacketConstant.DataOperateType.TCP_SOURCE_PORT, port)
    }

    fun getTargetPort(): Int {
        val targetPort = super.getAttribute(PacketConstant.DataOperateType.TCP_TARGET_PORT) ?: 0
        return targetPort as Int
    }

    fun setTargetPort(port: Int){
        super.setAttribute(PacketConstant.DataOperateType.TCP_TARGET_PORT, port)
    }

    fun isControlFlag(flag: ControlFlag): Boolean{
        val controlFlag = super.getAttribute(PacketConstant.DataOperateType.TCP_CONTROL_SIGN) ?: 0
        if (controlFlag is Byte){
            return flag.value and controlFlag == flag.value
        }
        return false
    }

    fun setControlFlag(flag: ControlFlag){
        val controlFlag = super.getAttribute(PacketConstant.DataOperateType.TCP_CONTROL_SIGN) ?: 0
        if (controlFlag is Byte){
            super.setAttribute(PacketConstant.DataOperateType.TCP_CONTROL_SIGN, flag.value or controlFlag)
        }
    }

    fun getSerialNumber(): Int {
        val serialNumber = super.getAttribute(PacketConstant.DataOperateType.TCP_SERIAL_NUMBER) ?: 0
        return serialNumber as Int
    }

    fun setSerialNumber(serialNumber: Int){
        super.setAttribute(PacketConstant.DataOperateType.TCP_SERIAL_NUMBER, serialNumber)
    }

    fun getVerifySerialNumber(): Int {
        val serialNumber = super.getAttribute(PacketConstant.DataOperateType.TCP_VERIFY_SERIAL_NUMBER) ?: 0
        return serialNumber as Int
    }

    fun setVerifySerialNumber(serialNumber: Int){
        super.setAttribute(PacketConstant.DataOperateType.TCP_VERIFY_SERIAL_NUMBER, serialNumber)
    }

    fun getOptionsData(): ByteArray {
        val data = super.getAttribute(PacketConstant.DataOperateType.TCP_OPTIONS) ?: ByteArray(0)
        return data as ByteArray
    }

    fun getOption(type: Option): ByteArray? {
        var result: ByteArray? = null
        val data = getOptionsData()
        var index = 0
        while (index < data.size){
            if (data[index] == Option.EOL.kind || data[index] == Option.NOP.kind){
                index++
                continue
            }
            if (data[index] == type.kind){
                val len = data[index + 1].toInt()
                if (index + len <= data.size){
                    result  = data.copyOfRange(index,index + len)
                }else{
                    Log.e(tag,"tcp 选项解析出现异常")
                }
                break
            }
            if (index + 1 < data.size){
                index += data[index + 1]
            }else{
                index++
            }
        }
        return result
    }

    fun addOption(type: Option, data: ByteArray) {
        val options = getOptionsData().toMutableList()
        when(type){
            Option.EOL,Option.NOP -> {
                options.add(type.kind)
            }
            Option.WSOPT -> {
                options.add(Option.NOP.kind)
                val len = type.len
                options.add(type.kind)
                options.add(type.len)
                for (i in 2 until len){
                    options.add(data[i - 2])
                }
            }
            Option.SACK_P -> {
                options.add(Option.NOP.kind)
                options.add(Option.NOP.kind)
                options.add(type.kind)
                options.add(type.len)
            }
            Option.MSS -> {
                val len = type.len
                options.add(type.kind)
                options.add(type.len)
                for (i in 2 until len){
                    options.add(data[i - 2])
                }
            }
            Option.TSOPT -> {
                options.add(Option.NOP.kind)
                options.add(Option.NOP.kind)
                val len = type.len
                options.add(type.kind)
                options.add(type.len)
                for (i in 2 until len){
                    options.add(data[i - 2])
                }
            }
            Option.FOC, Option.SACK -> {
                val len = 2 + data.size
                options.add(type.kind)
                options.add(len.toByte())
                for (i in data.indices){
                    options.add(data[i])
                }
            }
        }
        setAttribute(PacketConstant.DataOperateType.TCP_OPTIONS, options.toByteArray())
    }

    fun clearOptions(){
        setAttribute(PacketConstant.DataOperateType.TCP_OPTIONS, ByteArray(0))
    }

    /**
     * 设置最大报文段 Max Segment Size
     * 未设置时默认MSS为 536 byte
     * 通常在SYN时发送
     */
    fun setMSS(value: Int){
        val byteArray = ByteArray(2)
        byteArray[0] = ((value ushr 8) and 0xFF).toByte()
        byteArray[1] = (value and 0xFF).toByte()
        addOption(Option.MSS, byteArray)
    }

    @ExperimentalUnsignedTypes
    fun getMSS(): Int {
        val data = getOption(Option.MSS) ?: return 536

        if (data.size != 4) {
            return 536
        }

        return (data[2].toUByte().toInt() shl 8) or data[3].toUByte().toInt()
    }

    /**
     * 设置时间戳
     * @param tsval 当前的时间戳
     * @param tsecr 接收的时间戳
     */
    fun setTSOPT(tsecr: Long) {
        val tsval = (System.currentTimeMillis() / 1000).toInt()
        val byteArray = ByteArray(8)
        byteArray[0] = ((tsval ushr 24) and 0xFF).toByte()
        byteArray[1] = ((tsval ushr 16) and 0xFF).toByte()
        byteArray[2] = ((tsval ushr 8) and 0xFF).toByte()
        byteArray[3] = ((tsval) and 0xFF).toByte()

        byteArray[4] = ((tsecr ushr 24) and 0xFF).toByte()
        byteArray[5] = ((tsecr ushr 16) and 0xFF).toByte()
        byteArray[6] = ((tsecr ushr 8) and 0xFF).toByte()
        byteArray[7] = ((tsecr) and 0xFF).toByte()
        addOption(Option.TSOPT, byteArray)
    }

    @ExperimentalUnsignedTypes
    fun getTSOPT(): LongArray {
        val data = getOption(Option.TSOPT) ?: return longArrayOf(0,0)

        if (data.size != 10){
            return longArrayOf(0,0)
        }

        var newTsval = data[2].toUByte().toInt()
        newTsval = data[3].toUByte().toInt() or (newTsval shl 8)
        newTsval = data[4].toUByte().toInt() or (newTsval shl 8)
        newTsval = data[5].toUByte().toInt() or (newTsval shl 8)

        var newTsecr = data[6].toUByte().toInt()
        newTsecr = data[7].toUByte().toInt() or (newTsecr shl 8)
        newTsecr = data[8].toUByte().toInt() or (newTsecr shl 8)
        newTsecr = data[9].toUByte().toInt() or (newTsecr shl 8)

        return longArrayOf(newTsval.toUInt().toLong(), newTsecr.toUInt().toLong())
    }

    fun getSACK_P(): Boolean {
        val data = getOption(Option.SACK_P) ?: return false
        if (data.size != 2){
            return false
        }
        return true
    }

    fun setSACK_P(enable: Boolean){
        addOption(Option.SACK_P, byteArrayOf())
    }

    @ExperimentalUnsignedTypes
    fun getWSOPT(): Int {
        val data = getOption(Option.WSOPT) ?: return 0
        if (data.size != 3){
            return 0
        }
        return data[2].toUByte().toInt()
    }

    fun setWSOPT(size: Byte){
        addOption(Option.WSOPT, byteArrayOf(size))
    }

    fun getData(): ByteArray {
        val data = super.getAttribute(PacketConstant.DataOperateType.TCP_DATA) ?: ByteArray(0)
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

        super.setAttribute(PacketConstant.DataOperateType.TCP_DATA, data.copyOfRange(dataOffset, dataOffset + dataLength))
    }

    fun optionsToString() : String {
        val data = getOptionsData()
        if (data.isNotEmpty()){
            val sb = StringBuilder()
            getMSS().takeIf { it > 0 }?.let {
                sb.append("mss : $it \n")
            }
            getSACK_P().takeIf { it }?.let {
                sb.append("sack-p : $it \n")
            }
            getTSOPT().takeIf { it.size == 2 && it[0] != 0L }?.let {
                sb.append("tsopt : ${it[0]}:${it[1]} \n")
            }
            getWSOPT().takeIf { it > 0 }?.let {
                sb.append("wsopt : ${it} \n")
            }
            return sb.toString()
        }else{
            return "null"
        }
    }
}