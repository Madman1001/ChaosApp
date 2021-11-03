package com.lhr.sys.utils

import android.app.Activity
import android.app.Instrumentation
import com.lhr.sys.proxy.ProxyInstrumentation

/**
 * @author lhr
 * @date 2021/6/3
 * @des
 */
object HookUtil {
    var hooking = false
    fun hookInstrumentation(target: Activity) {
        try {
            val activityClass = Activity::class.java
            val ins = activityClass.getDeclaredField("mInstrumentation")
            ins.isAccessible = true
            val baseIns = ins.get(target) as Instrumentation
            ins.set(target, ProxyInstrumentation(baseIns))
        } catch (e: Exception) {
            throw RuntimeException("HookInstrumentation Fail")
        }
    }
}