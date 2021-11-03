package com.lhr.sys.reflection

/**
 * @author lhr
 * @date 2021/9/17
 * @des Field代理接口
 */
interface IProxyField {

    @Throws(IllegalArgumentException::class, IllegalAccessException::class)
    fun set(obj: Any?, value: Any?)

    @Throws(java.lang.IllegalArgumentException::class, IllegalAccessException::class)
    fun get(obj: Any?): Any?
}