package com.lhr.vpn.util

/**
 * @author lhr
 * @date 2021/11/16
 * @des 日志打印工具
 */
object ByteLog {
    fun toByteBufferString(array: ByteArray): String{
        return nativeGetByteBufferString(array)
    }

    private external fun nativeGetByteBufferString(bytes: ByteArray): String
}
