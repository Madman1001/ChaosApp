package com.lhr.vpn.protocol

/**
 * @author lhr
 * @date 2021/12/8
 * @des 协议接口
 */
interface IProtocol{
    fun getRawData(): ByteArray

    fun setRawData(byteArray: ByteArray)
}