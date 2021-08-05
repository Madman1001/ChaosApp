package com.example.myapplication

import android.app.Application
import android.util.Log

/**
 * @author lhr
 * @date 2021/8/5
 * @des
 */
class MyApplication : Application() {
    private val tag = this::class.java.simpleName
    override fun onCreate() {
        super.onCreate()
        Log.e(tag,"myUid = ${android.os.Process.myUid()}")
    }
}