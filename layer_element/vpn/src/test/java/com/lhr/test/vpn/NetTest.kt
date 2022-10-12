package com.lhr.test.vpn

import org.junit.Test

/**
 * @CreateDate: 2022/10/12
 * @Author: mac
 * @Description:
 */
class NetTest {

    @Test
    fun ipReadTest(){
        val i = 0x00001111

        println(i.toByte())
        println(i.toByte().toInt())
    }
}