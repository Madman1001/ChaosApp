package com.lhr.sys.compat

import android.os.IBinder

/**
 * @CreateDate: 2022/7/22
 * @Author: mac
 * @Description:
 */
class PackageManagerHookCompat {
    fun hookCompat(binder: IBinder){
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager")
        sPackageManagerField.isAccessible = true
        sPackageManagerField.set(null, null)
    }
}