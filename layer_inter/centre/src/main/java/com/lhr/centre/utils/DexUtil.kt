package com.lhr.centre.utils

import android.content.Context
import android.util.Log
import dalvik.system.DexFile

internal object DexUtil {
    const val tag = "DexUtil"

    fun getDexAllClassName(context: Context): Array<String>{
        val result = ArrayList<String>()
        try {
            val dexFile = DexFile(context.packageCodePath)
            val enumeration = dexFile.entries()
            while (enumeration.hasMoreElements()) {
                result.add(enumeration.nextElement())
            }
            dexFile.close()
        }catch (e: Exception){
            Log.e(tag,"查找dex文件出错！")
        }
        return result.toTypedArray()
    }
}