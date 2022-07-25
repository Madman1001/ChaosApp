package com.lhr.sys

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.lhr.sys.compat.ActivityManagerHookCompat
import com.lhr.sys.service.IUniversalHandler
import com.lhr.sys.service.ServiceHookBean
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy


/**
 * @CreateDate: 2022/7/21
 * @Author: mac
 * @Description:
 */
const val HOOK_TAG = "ServiceHook"

val SERVICE_HOOK_LIST: List<ServiceHookBean> get() {
    val result = mutableListOf<ServiceHookBean>()
    result.add(
        ServiceHookBean(Context.ALARM_SERVICE,
            Class.forName("android.app.IAlarmManager\$Stub"),
            Class.forName("android.app.IAlarmManager"))
    )
    result.add(
        ServiceHookBean(Context.APP_OPS_SERVICE,
            Class.forName("com.android.internal.app.IAppOpsService\$Stub"),
            Class.forName("com.android.internal.app.IAppOpsService"))
    )
    result.add(
        ServiceHookBean(Context.ACTIVITY_SERVICE,
            Class.forName("android.app.IActivityManager\$Stub"),
            Class.forName("android.app.IActivityManager")
        ){ binder ->
            ActivityManagerHookCompat().hookCompat(binder)
        }
    )
    result.add(
        ServiceHookBean("package",
            Class.forName("android.content.pm.IPackageManager\$Stub"),
            Class.forName("android.content.pm.IPackageManager"))
    )

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
        //黑名单api
        result.add(
            ServiceHookBean("activity_task",
                Class.forName("android.app.IActivityTaskManager\$Stub"),
                Class.forName("android.app.IActivityTaskManager"))
        )
    }
    return result
}

val HookFailList : MutableList<ServiceHookBean> = mutableListOf()