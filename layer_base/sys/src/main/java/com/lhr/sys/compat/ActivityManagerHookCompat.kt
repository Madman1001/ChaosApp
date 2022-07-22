package com.lhr.sys.compat

import android.app.ActivityManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.lhr.sys.service.HookBinderInvocationHandler

/**
 * @CreateDate: 2022/7/22
 * @Author: mac
 * @Description:
 */
class ActivityManagerHookCompat {
    fun hookCompat(binder: IBinder){
        val activityManagerClass = ActivityManager::class.java
        val iActivityManagerSingletonField = activityManagerClass.getDeclaredField("IActivityManagerSingleton")
        iActivityManagerSingletonField.isAccessible = true
        val iActivityManagerSingleton = iActivityManagerSingletonField.get(null)
        val singletonClass = Class.forName("android.util.Singleton")
        val mInstanceField = singletonClass.getDeclaredField("mInstance")
        mInstanceField.isAccessible = true
        mInstanceField.get(iActivityManagerSingleton) ?: return
        Handler(Looper.getMainLooper()).post {
            mInstanceField.set(iActivityManagerSingleton, HookBinderInvocationHandler.createBinderInvocation(binder, Class.forName("android.app.IActivityManager\$Stub")))
        }
    }
}