package com.example.sys

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
object PVMRuntime {
    private val vmRuntime by lazy {
        val obj = Class.forName("dalvik.system.VMRuntime")
        val method = obj.getDeclaredMethod("getRuntime")
        method.invoke(null)
    }

}