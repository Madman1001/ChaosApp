package com.lhr.sys.reflection

import java.lang.reflect.AccessibleObject
import java.lang.reflect.Method

/**
 * @author lhr
 * @date 2021/9/17
 * @des 系统代理方法
 */
class SysProxyMethod(val target: Class<*>, val methodString: String, vararg parameters: Class<*>) :
    IProxyMethod, SysReflectionBase() {
    /**
     * 执行方法
     */
    private val _InvokeMethod: Method by lazy {
        _FindMethod.invoke(
            Method::class.java,
            "invoke",
            arrayOf<Class<*>>(Any::class.java, Array<Any>::class.java)
        ) as Method
    }

    /**
     * 授予权限
     */
    private val _AccessMethod: Method by lazy {
        _FindMethod.invoke(
            AccessibleObject::class.java,
            "setAccessible",
            arrayOf(Boolean::class.java)
        ) as Method
    }

    private val targetMethod: Method by lazy {
        _FindMethod.invoke(target, methodString, parameters) as Method
    }

    /**
     * 执行目标方法
     */
    override fun invoke(obj: Any?, vararg args: Any?): Any? {
        _AccessMethod.invoke(targetMethod, true)
        return _InvokeMethod.invoke(targetMethod, obj, args)
    }

}