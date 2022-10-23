package com.lhr.vpn.socks.net.v4

/**
 * @author lhr
 * @date 2021/12/8
 * @des 协议接口
 */
object ChecksumUtil{
    /**
     * 计算校验和
     */
    fun checksum(data: ByteArray, len: Int): Int{
        var sum = 0
        for (i in 0 until len step 2){
            sum += (data[i].toInt() and 0xFF) shl 8
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        for (i in 1 until len step 2){
            sum += (data[i].toInt() and 0xFF)
            sum = (sum and 0xFFFF) + (sum shr 16)
        }

        sum = (sum and 0xFFFF) + (sum shr 16)
        return sum.inv()
    }
}