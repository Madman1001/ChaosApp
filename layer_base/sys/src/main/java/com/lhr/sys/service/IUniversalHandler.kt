package com.lhr.sys.service

import android.util.Log
import com.lhr.sys.HOOK_TAG
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * @CreateDate: 2022/7/21
 * @Author: mac
 * @Description: 透明动态代理类
 */
open class IUniversalHandler(
    protected var concrete: Any?
) : InvocationHandler {
    override fun invoke(proxy: Any?, method: Method, args: Array<out Any?>?): Any? {
        concrete?.let {
            kotlin.runCatching {
                val runner = it::class.java.getMethod(method.name, *method.parameterTypes)
                return runner.invoke(concrete, *(args ?: arrayOfNulls<Any>(0)))
            }
        }
        /**
         * 返回默认值
         */
        return when (method.returnType) {
            java.lang.Double.TYPE -> 0.0
            java.lang.Float.TYPE -> 0f
            java.lang.Character.TYPE -> '\u0000'
            java.lang.Boolean.TYPE -> false
            java.lang.Integer.TYPE -> 0
            java.lang.String::class.java -> ""
            java.lang.Void.TYPE -> null
            else -> null
        }
    }

    protected fun toLog(tag: String, proxy: Any, method: Method, args: Array<out Any?>?){
        val sb = StringBuilder()
        sb.append("call proxy ")
            .append(proxy::class.java.name)
            .append(".")
            .append(method.name)
            .append("(")
        if (args != null && args.isNotEmpty()){
            args.forEach {
                sb.append(it?.javaClass?.name)
                    .append(", ")
            }
            sb.delete(sb.length - 2, sb.length)
        }
        sb.append(")")
        Log.d("$HOOK_TAG-> $tag", sb.toString())
    }
}