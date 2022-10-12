package com.lhr.vpn.net

import java.nio.ByteBuffer

/**
 * @author lhr
 * @date 13/10/2022
 * @des 校验和
 */
object CheckSum {
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