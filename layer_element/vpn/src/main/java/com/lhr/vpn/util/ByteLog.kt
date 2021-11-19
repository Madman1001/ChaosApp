package com.lhr.vpn.util

import java.nio.ByteBuffer

/**
 * @author lhr
 * @date 2021/11/16
 * @des 日志打印工具
 */
object ByteLog {
    fun toByteBufferString(buffer: ByteBuffer, start: Int, end: Int): String{
        return nativeGetByteBufferString(buffer.array(),start, end)
    }

    private external fun nativeGetByteBufferString(bytes: ByteArray, start: Int, end: Int): String

    init {
        System.loadLibrary("byte-log-utils")
    }
}
