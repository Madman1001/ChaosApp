package com.example.access.utils

import android.os.Build
import com.example.access.bean.RomFeatureBean
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object RomMatchUtils {
    private const val TAG = "RomMatchUtils"

    private const val KEY_SDK_INT = "SDK_INT"
    private const val KEY_APP_VERSION = "APP_VERSION"
    private const val KEY_MANUFACTURER = "MANUFACTURER"
    private const val KEY_MODEL = "ro.product.model"

    private const val KEY_VERSION_OPPO = "ro.build.version.opporom"
    private const val KEY_VERSION_MIUI = "ro.miui.ui.version.name"
    private const val KEY_VERSION_EMUI = "ro.build.version.emui"
    private const val KEY_VERSION_VIVO = "ro.vivo.os.version"

    /**
     * 运算符号
     * == , < , >
     * <= , >=
     * left include , right include
     */
    private const val CONDITION_EQUAL = "equal"
    private const val CONDITION_LESS = "less"
    private const val CONDITION_GREATER = "greater"
    private const val CONDITION_LE = "le"
    private const val CONDITION_GE = "ge"
    private const val CONDITION_LFM = "lfm"
    private const val CONDITION_RFM = "rfm"

    /**
     * 查询结果缓存
     */
    private val cacheMap = HashMap<String,String>()

    /**
     * 判断是否符合条件
     */
    fun matchFeatureBean(bean: RomFeatureBean.FeatureBean): Boolean {
        val target = when (bean.key) {
            KEY_SDK_INT -> Build.VERSION.SDK_INT.toString()
            KEY_MANUFACTURER -> Build.MANUFACTURER
            KEY_MODEL -> Build.MODEL
            KEY_VERSION_OPPO,
            KEY_VERSION_MIUI,
            KEY_VERSION_EMUI,
            KEY_VERSION_VIVO -> getProp(bean.key)
            else -> getProp(bean.key)
        }
        val result = compareCondition(bean.condition, target, bean.value)
        return result
    }

    fun compareCondition(condition: String, varA: String, varB: String): Boolean {
        if (varA.isEmpty() || varB.isEmpty() || condition.isEmpty()){
            return false
        }
        return when (condition) {
            CONDITION_EQUAL -> {
                //等于
                return varA == varB
            }
            CONDITION_LESS -> {
                //小于
                try {
                    varA.toInt() < varB.toInt()
                }catch (e: Exception){
                    false
                }
            }
            CONDITION_LE -> {
                //小于等于
                try {
                    varA.toInt() <= varB.toInt()
                }catch (e: Exception){
                    false
                }
            }
            CONDITION_GREATER -> {
                //大于
                try {
                    varA.toInt() > varB.toInt()
                }catch (e: Exception){
                    false
                }
            }
            CONDITION_GE -> {
                //大于等于
                try {
                    varA.toInt() >= varB.toInt()
                }catch (e: Exception){
                    false
                }
            }

            CONDITION_LFM -> {
                //左包含
                varA.contains(varB)
            }
            CONDITION_RFM -> {
                //右包含
                varA.contains(varB)
            }
            else -> false
        }
    }


    fun getProp(name: String): String{
        var value = cacheMap[name]
        if (value == null){
            value = realGetProp(name)
            cacheMap[name] = value
        }
        return value
    }

    private fun realGetProp(name: String): String {
        var line = ""
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $name")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: IOException) {
            return ""
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return line
    }
}