package com.lhr.chaos

import android.util.Log
import java.util.*

/**
 * @CreateDate: 2022/7/28
 * @Author: mac
 * @Description:
 */
@Deprecated("该类用于字节码插桩")
object LogHelper {
    @JvmStatic
    private val sThreadLocal: ThreadLocal<Stack<Long>> = ThreadLocal()

    @JvmStatic
    public fun onMethodEnter(className: String?, methodName: String?, descriptor: String?) {
        var sStartTimeStack = sThreadLocal.get()
        if (sStartTimeStack == null) {
            sStartTimeStack = Stack()
            sThreadLocal.set(sStartTimeStack)
        }
        sStartTimeStack.push(System.currentTimeMillis())
    }

    @JvmStatic
    public fun onMethodExit(className: String?, methodName: String?, descriptor: String?){
        val sStartTimeStack = sThreadLocal.get() ?: return
        if (sStartTimeStack.isEmpty()) return

        val endTime = System.currentTimeMillis()
        val startTime = sStartTimeStack.pop()
        val costTime = endTime - startTime
        if (costTime > 0){
            Log.d("LogHelper", "method: ${className}_${methodName}${descriptor} cost $costTime ms")
        }
    }
}