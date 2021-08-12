package com.example.sys.reflection

import java.lang.reflect.Method

/**
 * @author lhr
 * @date 2021/6/7
 * @des
 */
object SysReflection {
    private val sysMethodReflection: Method =
        Class::class.java.getDeclaredMethod("getDeclaredMethod")

    fun runSysMethod(
        targetClass: Any,
        target: Any?,
        method: String,
        vararg params: Class<Any>?
    ): Boolean {
        return try {
            val hideMethod = sysMethodReflection.invoke(targetClass,method) as Method
            hideMethod.isAccessible = true
            hideMethod.invoke(target,*params)
            true
        }catch (e:Exception){
            e.printStackTrace()
            false
        }
    }
}