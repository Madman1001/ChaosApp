package com.example.sys.utils

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.util.Log

/**
 * @author lhr
 * @date 2021/10/9
 * @des app 工具
 */
object ApplicationUtil {
    //只能获取app自身服务
    private val app: Application? by lazy { ContextUtil.getApplicationByReflect() }
    fun getRunningServices(){
        app?.let {
            val am = it.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.getRunningServices(Int.MAX_VALUE)?.all {
                Log.e("Test",it.service.toString())
                true
            }
        }
    }

    //只能获取app自身task
    fun getRunningTasks(){
        app?.let {
            val am = it.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.getRunningTasks(Int.MAX_VALUE)?.all {
                Log.e("Test",it.topActivity.toString())
                true
            }
        }
    }
}