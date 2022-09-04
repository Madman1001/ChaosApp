package com.lhr.common.ext

import java.util.regex.Pattern

/**
 * @author lhr
 * @date 4/9/2022
 * @des
 */

private const val IPV4_REGEX: String = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$"

private val IPv4_PATTERN: Pattern = Pattern.compile(IPV4_REGEX)

fun String.isValidInet4Address(): Boolean{
    if (!IPv4_PATTERN.matcher(this).matches()){
        return false
    }

    val parts = this.split(".")
    kotlin.runCatching {
        for (part in parts) {
            if (part.toInt() > 255 || (part.length > 1 && part.startsWith("0"))){
                return false
            }
        }
    }.onFailure {
        return false
    }
    return true
}