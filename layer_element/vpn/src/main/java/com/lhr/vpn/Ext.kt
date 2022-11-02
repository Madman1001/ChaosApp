package com.lhr.vpn

/**
 * @author lhr
 * @date 22/10/2022
 * @des 扩展工具
 */
fun ByteArray.toHexString(): String {
    val sb = StringBuilder()
    for (i in this.indices) {
        if (i % 16 == 0) {
            sb.append(String.format("\n%4s", ""))
        }
        sb.append(java.lang.String.format(" %02X", this[i].toNetInt() and 0xFF))
    }
    return sb.toString()
}

fun ByteArray.toBinString(): String {
    val sb = StringBuilder()
    for (i in this.indices) {
        if (i % 16 == 0) {
            sb.append(String.format("\n%4s", ""))
        }
        val str = this[i].toUByte().toString(2)
        for (j in 0 until (8-str.length)){
            sb.append(0)
        }
        sb.append(str)
        sb.append(String.format("%1s", ""))
    }
    return sb.toString()
}

fun ByteArray.getShort(offset: Int): Short{
    return ((this[offset].toNetInt() shl 8) or this[offset + 1].toNetInt()).toShort()
}

fun ByteArray.setShort(offset: Int, value: Short){
    this[offset] = (value.toNetInt() ushr 8).toByte()
    this[offset + 1] = (value.toNetInt() and 0xFF).toByte()
}

fun ByteArray.getInt(offset: Int): Int {
    var value = 0
    value = (this[offset].toNetInt() shl 24) or value
    value = (this[offset + 1].toNetInt() shl 16) or value
    value = (this[offset + 2].toNetInt() shl 8) or value
    value = (this[offset + 3].toNetInt()) or value
    return value
}

fun ByteArray.setInt(offset: Int, value: Int) {
    this[offset] = (value ushr 24 and 0xFF).toByte()
    this[offset + 1] = (value ushr 16 and 0xFF).toByte()
    this[offset + 2] = (value ushr 8 and 0xFF).toByte()
    this[offset + 3] = (value and 0xFF).toByte()
}

fun Int.toIpString(): String{
    val sb = StringBuilder()
        .append(this ushr 24 and 0xFF)
        .append(".")
        .append(this ushr 16 and 0xFF)
        .append(".")
        .append(this ushr 8 and 0xFF)
        .append(".")
        .append(this and 0xFF)
    return sb.toString()
}

fun String.toIpInt(): Int{
    val list = this.split(".")
    var value = 0
    value = (list[0].toInt() shl 24 and 0xFF000000.toInt()) or value
    value = (list[1].toInt() shl 16 and 0x00FF0000) or value
    value = (list[2].toInt() shl 8 and 0x0000FF00) or value
    value = (list[3].toInt() and 0x000000FF) or value
    return value
}

fun Short.toNetInt(): Int{
    return this.toUShort().toInt()
}

fun Byte.toNetInt(): Int{
    return this.toUByte().toInt()
}