package com.lhr.sys

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.AppOpsManager
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import com.lhr.centre.annotation.CElement

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "系统代理")
class SysActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sys_activity)
    }

    fun getHookAlarmManager(v: View){
        (this.getSystemService(ALARM_SERVICE) as AlarmManager).nextAlarmClock
    }

    fun getHookPackageManager(v: View){
        this.packageManager.isSafeMode
    }

    fun getHookActivityManager(v: View){
        (this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).appTasks
    }

    fun getHookAppOpsService(v: View){
        (this.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager)
            .checkPackage(Process.myUid(), this.packageName)
    }

    @SuppressLint("HardwareIds")
    fun getAndroidId(v: View){
        val mAndroidId = Settings.Secure.getString(contentResolver, "android_id")
        Log.e(HOOK_TAG,"android id $mAndroidId")
    }

    fun getMacId(v: View){
        val mac = (this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo?.macAddress
        Log.e(HOOK_TAG,"mac $mac")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTelephony(v: View){
        val telephony = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val imei = telephony.imei
        Log.e(HOOK_TAG,"imei $imei")
    }
}