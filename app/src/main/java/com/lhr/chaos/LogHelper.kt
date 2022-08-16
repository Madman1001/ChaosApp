package com.lhr.chaos

import android.util.Log

/**
 * @CreateDate: 2022/7/28
 * @Author: mac
 * @Description:
 */
object LogHelper {
    @JvmStatic
    private val sThreadLocal: ThreadLocal<HashMap<String, Long>> = ThreadLocal()

    @JvmStatic
    public fun onMethodEnter(className: String?, methodName: String?) {
        var sStartTime = sThreadLocal.get()
        if (sStartTime == null) {
            sStartTime = HashMap<String, Long>()
            sThreadLocal.set(sStartTime)
        }
        sStartTime[className + "_" + methodName] = System.currentTimeMillis()
    }

    @JvmStatic
    public fun onMethodExit(className: String?, methodName: String?){
        val sStartTime = sThreadLocal.get() ?: return
        val endTime = System.currentTimeMillis()
        val startTime = sStartTime[className + "_" + methodName] ?: System.currentTimeMillis()
        val costTime = endTime - startTime
        if (costTime > 0){
            Log.d("LogHelper", "method: ${className}_$methodName cost ${costTime} ms")
        }
    }
}