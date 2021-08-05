package com.example.sys.proxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * @author lhr
 * @date 2021/8/4
 * @des
 */
object WrapperCallbackFactory {
    fun generateWrapperCallback(callback: Any): Any?{
        val proxy = ProxyCallback(callback)
        return Proxy.newProxyInstance(callback::class.java.classLoader,callback::class.java.interfaces,proxy)
    }

    class ProxyCallback(private var concrete: Any?) : InvocationHandler {

        override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
            concrete?.let {
                val runner = it::class.java.getMethod(method.name, *method.parameterTypes)
                println("Proxy: " + method.name + " : " + args)
                return runner.invoke(concrete,*(args ?: arrayOfNulls<Any>(0)))
            }
            return null
        }
    }
}