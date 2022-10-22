package com.lhr.vpn.ext

/**
 * @author lhr
 * @date 22/10/2022
 * @des 字节数组扩展工具
 */
fun ByteArray.hexToString(): String {
    val sb = StringBuilder()
    for (i in this.indices) {
        if (i % 16 == 0) {
            sb.append(String.format("\n%4s", ""))
        }
        sb.append(java.lang.String.format(" %02X", this[i].toUByte().toInt() and 0xFF))
    }
    return sb.toString()
}