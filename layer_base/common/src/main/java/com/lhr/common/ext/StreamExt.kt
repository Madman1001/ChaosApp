package com.lhr.common.ext

import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

/**
 * @CreateDate: 2022/8/26
 * @Author: mac
 * @Description:
 */

/**
 * 输入流读取字符串扩展
 */
fun InputStream.readText(): String{
    return InputStreamReader(this).readText()
}

fun File.readText(): String{
    return this.inputStream().readText()
}