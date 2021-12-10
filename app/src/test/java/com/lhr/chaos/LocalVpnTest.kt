package com.lhr.chaos

import org.junit.Test
/**
 * @author lhr
 * @date 2021/12/5
 * @des 地址测试
 */
class LocalVpnTest {

    @Test
    fun numTest(){
        val sb = StringBuilder()
        var address = 3232236281
        for (i in 0 until 4) {
            sb.insert(0,address and 0xFF)
            if (i != 3){
                sb.insert(0,'.')
            }
            address = address ushr 8
        }
        println(sb.toString())
    }

    @Test
    fun ipParse(){
        val address = "192.168.2.249"
        val addressList = address.split(".")
        var addr = 0x00000000
        for (subAddr in addressList) {
            addr = addr shl 8
            addr = addr or subAddr.toInt()
        }
        println(addr.toUInt())
    }
}