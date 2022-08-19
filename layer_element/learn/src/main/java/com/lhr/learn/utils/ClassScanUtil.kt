package com.lhr.learn.utils

import android.content.Context
import android.util.Log
import dalvik.system.BaseDexClassLoader
import dalvik.system.DexFile
import java.lang.reflect.Field

/**
 * @CreateDate: 2022/8/19
 * @Author: mac
 * @Description: class 扫描工具
 */
object ClassScanUtil {
    const val tag = "ClassScanUtil"

    fun getAllClass(context: Context): List<String>{
        val result = mutableListOf<String>()
        for (dexFile in getDexFiles(context)) {
            result.addAll(getDexAllClassName(dexFile))
        }
        return result
    }

    internal fun getDexAllClassName(dexFile: DexFile): Array<String>{
        val result = ArrayList<String>()
        try {
            val enumeration = dexFile.entries()
            while (enumeration.hasMoreElements()) {
                result.add(enumeration.nextElement())
            }
        }catch (e: Exception){
            Log.e(tag,"查找dex文件出错！")
        }
        return result.toTypedArray()
    }

    /**
     * 获取所以dex文件
     */
    internal fun getDexFiles(context: Context): Sequence<DexFile> {
        // Here we do some reflection to access the dex files from the class loader. These implementation details vary by platform version,
        // so we have to be a little careful, but not a huge deal since this is just for testing. It should work on 21+.
        // The source for reference is at:
        // https://android.googlesource.com/platform/libcore/+/oreo-release/dalvik/src/main/java/dalvik/system/BaseDexClassLoader.java
        val classLoader = context.classLoader as BaseDexClassLoader

        val pathListField = field("dalvik.system.BaseDexClassLoader", "pathList")
        val pathList = pathListField.get(classLoader) // Type is DexPathList

        val dexElementsField = field("dalvik.system.DexPathList", "dexElements")
        @Suppress("UNCHECKED_CAST")
        val dexElements = dexElementsField.get(pathList) as Array<Any> // Type is Array<DexPathList.Element>

        val dexFileField = field("dalvik.system.DexPathList\$Element", "dexFile")
        return dexElements.map {
            dexFileField.get(it) as DexFile
        }.asSequence()
    }

    private fun field(className: String, fieldName: String): Field {
        val clazz = Class.forName(className)
        val field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        return field
    }
}