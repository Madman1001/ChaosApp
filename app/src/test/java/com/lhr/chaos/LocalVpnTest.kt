package com.lhr.chaos

import com.lhr.vpn.protocol.TCPPacket
import org.junit.Test
/**
 * @author lhr
 * @date 2021/12/5
 * @des 地址测试
 */
class LocalVpnTest {

    @ExperimentalUnsignedTypes
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

        val tsval = (System.currentTimeMillis() / 1000).toInt()
        val tsecr = (System.currentTimeMillis() / 1000).toInt()
        println("$tsval  $tsecr")
        val byteArray = ByteArray(8)
        byteArray[0] = ((tsval ushr 24) and 0xFF).toByte()
        byteArray[1] = ((tsval ushr 16) and 0xFF).toByte()
        byteArray[2] = ((tsval ushr 8) and 0xFF).toByte()
        byteArray[3] = ((tsval) and 0xFF).toByte()

        byteArray[4] = ((tsecr ushr 24) and 0xFF).toByte()
        byteArray[5] = ((tsecr ushr 16) and 0xFF).toByte()
        byteArray[6] = ((tsecr ushr 8) and 0xFF).toByte()
        byteArray[7] = ((tsecr) and 0xFF).toByte()

        var newTsval = byteArray[0].toUByte().toInt()
        newTsval = byteArray[1].toUByte().toInt() or (newTsval shl 8)
        newTsval = byteArray[2].toUByte().toInt() or (newTsval shl 8)
        newTsval = byteArray[3].toUByte().toInt() or (newTsval shl 8)

        var newTsecr = byteArray[4].toUByte().toInt()
        newTsecr = byteArray[5].toUByte().toInt() or (newTsecr shl 8)
        newTsecr = byteArray[6].toUByte().toInt() or (newTsecr shl 8)
        newTsecr = byteArray[7].toUByte().toInt() or (newTsecr shl 8)
        println("$newTsval  $newTsecr")
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