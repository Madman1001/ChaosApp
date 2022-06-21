package com.lhr.sys.utils

import android.app.Activity
import android.app.Instrumentation
import android.util.Log
import com.lhr.sys.proxy.ProxyInstrumentation

/**
 * @author lhr
 * @date 2021/6/3
 * @des
 */
object HookUtil {
    fun hookInstrumentation(target: Activity, proxyInstrumentation: Class<*>) {
        try {
            val activityClass = Activity::class.java
            val ins = activityClass.getDeclaredField("mInstrumentation")
            ins.isAccessible = true
            val baseIns = ins.get(target) as Instrumentation
            ins.set(target, proxyInstrumentation.getConstructor(Instrumentation::class.java).newInstance(baseIns))
        } catch (e: Exception) {
            throw RuntimeException("HookInstrumentation Fail")
        }
    }
}