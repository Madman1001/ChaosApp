package com.lhr.vpn.util

/**
 * @author lhr
 * @date 2021/11/16
 * @des 日志打印工具
 */
object ByteLog {
    fun binaryToString(array: ByteArray): String {
        return nativeBinaryToString(array)
    }

    fun hexToString(array: ByteArray): String {
        return nativeHexToString(array)
    }

    private external fun nativeBinaryToString(bytes: ByteArray): String

    private external fun nativeHexToString(bytes: ByteArray): String
}
