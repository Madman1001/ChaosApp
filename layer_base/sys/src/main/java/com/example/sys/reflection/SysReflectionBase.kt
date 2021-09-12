package com.example.sys.reflection

import java.lang.reflect.Method

/**
 * @author lhr
 * @date 2021/9/17
 * @des 系统反射基础类
 */
abstract class SysReflectionBase {

    /**
     * 查询方法
     */
    protected val _FindMethod: Method by lazy {
        Class::class.java.getMethod("getDeclaredMethod", String::class.java, Array::class.java)
    }

    /**
     * 查询属性
     */
    protected val _FindFeild: Method by lazy {
        Class::class.java.getMethod("getDeclaredField", String::class.java)
    }
}