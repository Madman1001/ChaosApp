package com.lhr.vpn.net

import java.nio.ByteBuffer

/**
 * @author lhr
 * @date 2021/12/8
 * @des 协议接口
 */
interface IProtocol{
    fun decodePacket(buffer: ByteBuffer)
    fun encodePacket(): ByteBuffer
}