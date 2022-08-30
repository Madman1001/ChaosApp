package com.lhr.common.ext

/**
 * @CreateDate: 2022/6/29
 * @Author: mac
 * @Description: byte kb mb gb 单位转换
 */
const val GB = 1073741824L
const val MB = 1048576L
const val KB = 1024L

fun Long.byteToGb(): Double {
    return (this * 1.0) / GB
}

fun Long.byteToMb(): Double {
    return (this * 1.0) / MB
}

fun Long.byteToKb(): Double {
    return (this * 1.0) / KB
}
