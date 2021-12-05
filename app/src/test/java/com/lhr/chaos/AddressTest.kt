package com.lhr.chaos

import org.junit.Test
/**
 * @author lhr
 * @date 2021/12/5
 * @des 地址测试
 */
class AddressTest {

    @Test fun numTest(){
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
}