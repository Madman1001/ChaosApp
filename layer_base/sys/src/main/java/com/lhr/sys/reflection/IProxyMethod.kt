package com.lhr.sys.reflection

import java.lang.reflect.InvocationTargetException

/**
 * @author lhr
 * @date 2021/9/17
 * @des Method代理接口
 */
interface IProxyMethod {

    @Throws(
        IllegalAccessException::class,
        IllegalArgumentException::class,
        InvocationTargetException::class
    )
    fun invoke(obj: Any?, vararg args: Any?): Any?
}