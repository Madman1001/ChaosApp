package com.lhr.vpn.protocol

/**
 * @author lhr
 * @date 2021/11/16
 * @des ip 数据报
 */
class IPPacket(private val bytes: ByteArray) {
    init {
        nativeInit(bytes)
    }


    private external fun nativeInit(bytes: ByteArray)
}