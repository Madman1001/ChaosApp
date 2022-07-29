package com.lhr.sys

import android.content.Context
import android.os.Build
import com.lhr.sys.compat.ActivityManagerHookCompat
import com.lhr.sys.compat.PackageManagerHookCompat
import com.lhr.sys.service.ServiceHookBean


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
            Class.forName("android.content.pm.IPackageManager")
        ){binder ->
            PackageManagerHookCompat().hookCompat(binder)
        }
    )

    result.add(
         ServiceHookBean(Context.WIFI_SERVICE,
            Class.forName("android.net.wifi.IWifiManager\$Stub"),
            Class.forName("android.net.wifi.IWifiManager"))
    )

    result.add(
         ServiceHookBean(Context.TELEPHONY_SERVICE,
            Class.forName("com.android.internal.telephony.ITelephony\$Stub"),
            Class.forName("com.android.internal.telephony.ITelephony")
        )
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