package com.lhr.common.utils

import android.os.Build
import java.io.File


/**
 * @author lhr
 * @date 2021/11/20
 * @des 设备相关工具
 */
object MachineUtils {

    /**
     * 获取CPU个数
     */
    fun getCpuNum() : Int = Runtime.getRuntime().availableProcessors()

    /**
     * 判断是否有root权限
     */
    fun isRoot(): Boolean = isRoot1() || isRoot2() || isRoot3()

    private fun isRoot1(): Boolean {
        val str = Build.TAGS
        return str != null && str.contains("test-keys")
    }

    private fun isRoot2(): Boolean {
        return File("/system/app/Superuser.apk").exists()
    }

    private fun isRoot3(): Boolean {
        for (file in arrayOf(
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )) {
            if (File(file).exists()) {
                return true
            }
        }
        return false
    }
}