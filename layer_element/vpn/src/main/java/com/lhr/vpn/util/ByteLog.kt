package com.lhr.vpn.util

/**
 * @author lhr
 * @date 2021/11/16
 * @des 日志打印工具
 */
object ByteLog {
    fun hexToString(array: ByteArray): String {
        val sb = StringBuilder()
        for (i in array.indices) {
            if (i % 16 == 0) {
                sb.append(String.format("\n%4s", ""))
            }
            sb.append(java.lang.String.format(" %02X", array[i].toUByte().toInt() and 0xFF))
        }
        return sb.toString()
    }
}
