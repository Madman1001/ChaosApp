package com.lhr.sys.service

import android.os.IBinder
import java.lang.reflect.Method

/**
 * @CreateDate: 2022/7/22
 * @Author: mac
 * @Description:
 */
class HookBinderInvocationHandler(base: IBinder, stubClass: Class<*>) : IUniversalHandler(base) {
    private val rawService: Any?
    init {
        rawService = createBinderInvocation(base, stubClass)
        concrete = rawService
    }

    companion object{
        fun createBinderInvocation(base: IBinder, stubClass: Class<*>): Any? {
            val asInterfaceMethod =
                stubClass.getDeclaredMethod("asInterface", IBinder::class.java)
            return asInterfaceMethod.invoke(null, base)
        }
    }

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
        rawService?.run {
            toLog(this::class.java.simpleName, rawService, method, args)
        }
        return super.invoke(proxy, method, args)
    }
}