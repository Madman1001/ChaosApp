package com.lhr.common.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.text.TextUtils

/**
 * @CreateDate: 2022/8/30
 * @Author: mac
 * @Description:
 */
object ProcessUtils {
    fun isMainProcess(context: Context): Boolean {
        return context.packageName == getProcessName(context)
    }

    fun getProcessName(context: Context): String {
        var processName = getProcessNameByApplication()
        if (TextUtils.isEmpty(processName)) {
            processName = getProcessNameByReflection()
        }
        if (TextUtils.isEmpty(processName)) {
            processName = getProcessNameByActivityManager(context)
        }
        return processName
    }

    fun getProcessNameByApplication(): String {
        var processName = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            processName = Application.getProcessName()
        }
        return processName
    }

    @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
    fun getProcessNameByReflection(): String {
        var processName = ""
        try {
            val declaredMethod = Class.forName(
                "android.app.ActivityThread",
                false,
                Application::class.java.classLoader
            ).getDeclaredMethod("currentProcessName")
            declaredMethod.isAccessible = true
            val obj = declaredMethod.invoke(null)
            if (obj is String) {
                processName = obj
            }
        } catch (e: Throwable) {
            processName = ""
        }
        return processName
    }

    fun getProcessNameByActivityManager(context: Context): String {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val myPid = Process.myPid()
        for (process in manager.runningAppProcesses) {
            if (process.pid == myPid) {
                return process.processName
            }
        }
        return ""
    }
}