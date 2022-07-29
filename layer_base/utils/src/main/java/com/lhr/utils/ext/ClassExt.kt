package com.lhr.utils.ext

import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * @CreateDate: 2022/7/27
 * @Author: mac
 * @Description:
 */
val unsafe: Any = Class.forName("sun.misc.Unsafe").getDeclaredMethod("getUnsafe").invoke(null)
val objectFieldOffset: Method = unsafe.javaClass.getDeclaredMethod("objectFieldOffset")

fun Class<*>.sizeOf(): Long{
    var maximumOffset = 0L
    var tClass: Class<*>? = this
    do {
        for (field in tClass?.declaredFields ?: break) {
            if (!Modifier.isStatic(field.modifiers)) {
                maximumOffset = maximumOffset.coerceAtLeast(objectFieldOffset.invoke(unsafe, field) as Long)
            }
        }
        tClass = tClass.superclass
    } while (tClass != null)
    return maximumOffset + 8
}