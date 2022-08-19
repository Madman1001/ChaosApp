package com.lhr.common.utils

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * @author lhr
 * @date 2021/11/20
 * @des io操作工具
 */
object IOUtils {
    fun readStream(input: InputStream) : String {
        val sb: StringBuilder = StringBuilder()
        val reader = BufferedReader(InputStreamReader(input))
        val buffer = CharArray(512)
        var len = reader.read(buffer)
        while (len != -1){
            sb.append(buffer,0,len)
            len = reader.read(buffer)
        }
        return sb.toString()
    }
}