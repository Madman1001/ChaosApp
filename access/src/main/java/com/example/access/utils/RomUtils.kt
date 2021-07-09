package com.example.access.utils

import android.os.Build
import android.text.TextUtils
import android.util.Log

/**
 * @author lhr
 * @date 2021/7/9
 * @des Rom信息读取工具
 */
object RomUtils {
    private val _tag = this::class.java.simpleName
    private var f27403b = -1
    private var romId = 902
    @Synchronized
    fun getRomId(z: Boolean): Int {
        try {
            if (this.f27403b != -1) {
                this.romId = this.f27403b
            } else if (this.romId == 902 || 0.toInt() == 0) {
                this.romId = generateRomId()
            }
        } catch (th: Throwable) {
            th.printStackTrace()
        }
        Log.e(_tag,"getRomId=$romId")
        return this.romId
    }

    private fun generateRomId(): Int {
        val str = Build.BRAND
        if (TextUtils.isEmpty(str)) {
            return 902
        }
        val upperCase = str.toUpperCase()
        var c = 65535.toChar()
        if (upperCase.hashCode() == 2634924 && upperCase == "VIVO") {
            c = 0.toChar()
        }
        if (c.toInt() != 0) {
            return 902
        }
        return 1000
    }
}