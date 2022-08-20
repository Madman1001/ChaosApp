package com.example.crash

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

/**
 * @CreateDate: 2022/4/29
 * @Author: mac
 * @Description: 崩溃捕捉工具
 */
class ChaosCrashHandler(private val app: Context) : Thread.UncaughtExceptionHandler {
    private val exceptionHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        e.printStackTrace()
        Log.e("ChaosCrashHandler", e.stackTraceToString())
        gotoCrashActivity(e.stackTraceToString())
    }

    private fun gotoCrashActivity(crashDetail: String){

        Intent(app, CrashActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(CrashActivity.CRASH_DETAIL_KEY, crashDetail)
            app.startActivity(this)
        }
    }
}