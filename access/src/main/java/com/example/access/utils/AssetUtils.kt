package com.example.access.utils

import android.content.Context
import android.content.res.AssetManager
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * @author lhr
 * @date 2021/7/8
 * @des
 */
object AssetUtils {
    private fun getAccessConfig(code: Int,context: Context): InputStream? {
        val assets: AssetManager = context.resources.assets
        val sb = StringBuilder()
        sb.append("accessconfigs/")
        sb.append(code)
        sb.append(".json")
        return try {
            assets.open(sb.toString())
        } catch (unused: FileNotFoundException) {
            try {
                assets.open("accessconfigs/902.json")
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } catch (e2: Exception) {
            e2.printStackTrace()
            null
        }
    }
}