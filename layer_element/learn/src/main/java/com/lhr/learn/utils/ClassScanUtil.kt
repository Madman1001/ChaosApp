package com.lhr.learn.utils

import android.content.Context
import android.util.Log
import dalvik.system.BaseDexClassLoader
import dalvik.system.DexFile
import java.lang.reflect.Field
import java.util.jar.JarFile

/**
 * @CreateDate: 2022/8/19
 * @Author: mac
 * @Description: class 扫描工具
 */
object ClassScanUtil {
    const val tag = "ClassScanUtil"

    fun getAllClass(context: Context): List<String> {
        val result = mutableListOf<String>()
        kotlin.runCatching {
            for (dexFile in getDexFilesByContext(context)) {
                Log.e(tag, "Dex File By Context ${dexFile.name}")
                result.addAll(getDexAllClassName(dexFile))
            }
        }.onFailure {
            it.printStackTrace()
        }

        kotlin.runCatching {
            for (dexFile in getDexFilesBySystem()) {
                Log.e(tag, "Dex File By System ${dexFile.name}")
                result.addAll(getDexAllClassName(dexFile))
                dexFile.close()
            }
        }.onFailure {
            it.printStackTrace()
        }

        return result
    }

    internal fun getDexAllClassName(dexFile: DexFile): Array<String> {
        val result = ArrayList<String>()
        try {
            val enumeration = dexFile.entries()
            while (enumeration.hasMoreElements()) {
                result.add(enumeration.nextElement())
            }
        } catch (e: Exception) {
            Log.e(tag, "查找dex文件出错！")
        }
        return result.toTypedArray()
    }

    /**
     * 获取虚拟机内部jar文件
     * VMClassLoader为隐藏对象，需要绕过安全检查
     */
    internal fun getDexFilesBySystem(): Sequence<DexFile> {
        val bootClassPathUrlHandlersField =
            field("java.lang.VMClassLoader", "bootClassPathUrlHandlers")
        val bootClassPathUrlHandlers = bootClassPathUrlHandlersField.get(null)
        val jarFileField = field("libcore.io.ClassPathURLStreamHandler", "jarFile")
        val jarFiles = (bootClassPathUrlHandlers as Array<*>).map {
            jarFileField.get(it) as JarFile
        }
        return jarFiles.map {
            val result = kotlin.runCatching {
                //todo 系统内部文件可能会被加密，无法直接使用dexfile解析
                DexFile(it.name)
            }
            result.getOrNull()
        }.filterNotNull().asSequence()
    }

    /**
     * 获取安装包的dex文件
     */
    internal fun getDexFilesByContext(context: Context): Sequence<DexFile> {
        val classLoader = context.classLoader as BaseDexClassLoader

        val pathListField = field("dalvik.system.BaseDexClassLoader", "pathList")
        val pathList = pathListField.get(classLoader) // Type is DexPathList

        val dexElementsField = field("dalvik.system.DexPathList", "dexElements")

        @Suppress("UNCHECKED_CAST")
        val dexElements =
            dexElementsField.get(pathList) as Array<Any> // Type is Array<DexPathList.Element>

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