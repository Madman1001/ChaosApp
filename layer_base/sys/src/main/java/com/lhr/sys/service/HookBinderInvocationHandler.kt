package com.lhr.sys.service

import android.os.Build
import android.os.IBinder
import com.lhr.sys.reflection.HiddenApiBypass
import java.lang.reflect.Method

/**
 * @CreateDate: 2022/7/22
 * @Author: mac
 * @Description:
 */
class HookBinderInvocationHandler(private val base: IBinder, private val stubClass: Class<*>) : IUniversalHandler(base) {
    private val rawService: Any? = createBinderInvocation(base, stubClass)
    init {
        concrete = rawService
    }

    companion object{
        fun createBinderInvocation(base: IBinder, stubClass: Class<*>): Any? {
            kotlin.runCatching {
                val asInterfaceMethod =
                    stubClass.getDeclaredMethod("asInterface", IBinder::class.java)
                return asInterfaceMethod.invoke(null, base)
            }.onFailure {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    HiddenApiBypass.invoke(stubClass, null, "asInterface", base)
                } else {
                    null
                }
            }
            return null
        }
    }

    override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
        rawService?.run {
            toLog(this::class.java.simpleName, rawService, method, args)
        }

        return super.invoke(rawService, method, args)
    }
}