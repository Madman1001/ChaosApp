package com.lhr.learn.utils

import android.content.Context
import dalvik.system.BaseDexClassLoader
import java.io.File
import java.lang.reflect.Field

/**
 * @CreateDate: 2022/8/30
 * @Author: mac
 * @Description: 获取所有so 文件
 */
object NativeLibScanUtil {
    const val tag = "NativeLibScanUtil"

    fun getAllNativeLibraryFile(context: Context): List<File> {
        val classLoader = context.classLoader as BaseDexClassLoader
        val pathListField = field("dalvik.system.BaseDexClassLoader", "pathList")
        val pathList = pathListField.get(classLoader) // Type is DexPathList
        val nativeLibraryField = field("dalvik.system.DexPathList", "nativeLibraryDirectories")
        val systemNativeLibraryField =
            field("dalvik.system.DexPathList", "systemNativeLibraryDirectories")

        val allNativeLibraryDirectories: MutableList<File> = ArrayList()
        @Suppress("UNCHECKED_CAST")
        allNativeLibraryDirectories.addAll(nativeLibraryField.get(pathList) as List<File>)
        @Suppress("UNCHECKED_CAST")
        allNativeLibraryDirectories.addAll(systemNativeLibraryField.get(pathList) as List<File>)
        return allNativeLibraryDirectories
    }

    private fun field(className: String, fieldName: String): Field {
        val clazz = Class.forName(className)
        val field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        return field
    }
}