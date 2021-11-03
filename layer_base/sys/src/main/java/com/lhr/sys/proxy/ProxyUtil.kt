package com.lhr.sys.proxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * @author lhr
 * @date 2021/8/6
 * @des 接口代理工具
 */
inline fun <reified T> Any.wrapper(): T {
    if (this is T) {
        val wrapper = ProxyCallback(this)
        return Proxy.newProxyInstance(
            this::class.java.classLoader,
            arrayOf(T::class.java),
            wrapper
        ) as T
    } else {
        throw Exception("${this.javaClass} not implement interface ${T::class.java}")
    }
}

class ProxyCallback(private var concrete: Any?) : InvocationHandler {
    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
        concrete?.let {
            println(this::class.java.simpleName + " >>> " + it::class.java.simpleName + " " + method.name)
            val runner = it::class.java.getMethod(method.name, *method.parameterTypes)
            return runner.invoke(concrete, *(args ?: arrayOfNulls<Any>(0)))
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
}