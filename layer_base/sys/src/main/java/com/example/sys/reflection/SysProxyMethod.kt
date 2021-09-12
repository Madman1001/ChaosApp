package com.example.sys.reflection

import java.lang.reflect.AccessibleObject
import java.lang.reflect.Method

/**
 * @author lhr
 * @date 2021/9/17
 * @des 系统代理方法
 */
class SysProxyMethod(val target: Class<*>, val methodString: String, vararg parameters: Class<*>) : IProxyMethod, SysReflectionBase() {
    /**
     * 执行方法
     */
    val _InvokeMethod: Method by lazy {
        _FindMethod.invoke(Method::class.java,"invoke",Any::class.java, Array::class.java) as Method
    }

    /**
     * 授予权限
     */
    val _AccessMethod: Method by lazy {
        _FindMethod.invoke(AccessibleObject::class.java,"setAccessible",Boolean::class.java) as Method
    }

    private val method: Method = _FindMethod.invoke(target,methodString,*parameters) as Method

    override fun invoke(obj: Any?, vararg args: Any?): Any? {
        _AccessMethod.invoke(method,true)
        return _InvokeMethod.invoke(method,obj,*(args ?: arrayOfNulls<Any>(0)))
    }

}