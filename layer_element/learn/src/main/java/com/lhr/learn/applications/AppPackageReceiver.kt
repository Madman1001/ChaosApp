package com.lhr.learn.applications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

/**
 * @CreateDate: 2022/6/29
 * @Author: mac
 * @Description: 应用安装、卸载广播接收器
 */
class AppPackageReceiver(private val callback: (Context?,Intent?) -> Unit = {_,_ ->}): BroadcastReceiver() {
    private val TAG = this::class.java.simpleName
    private var isRegister = false
    private var appContext: Context? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        callback.invoke(context, intent)
    }

    @Synchronized
    fun register(context: Context){
        if (isRegister) return
        appContext = context.applicationContext
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        intentFilter.addDataScheme("package")
        appContext?.registerReceiver(this, intentFilter)
        isRegister = true
    }

    @Synchronized
    fun unregister(){
        if (!isRegister) return
        appContext?.unregisterReceiver(this)
        isRegister = false
    }
}