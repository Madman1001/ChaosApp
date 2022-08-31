package com.lhr.sys

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.AppOpsManager
import android.content.Context
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import com.lhr.centre.annotation.CElement
import com.lhr.common.ui.BaseActivity
import com.lhr.common.ui.BaseAdapter
import com.lhr.sys.databinding.SysActivityBinding
import com.lhr.sys.service.ServiceHookBean
import java.lang.reflect.Proxy

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "系统服务代理")
class SysActivity : BaseActivity<SysActivityBinding>() {
    private val tag = "AS_${this::class.java.simpleName}"

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        mBinding.listRv.run {
            layoutManager = GridLayoutManager(this.context, 3, GridLayoutManager.VERTICAL, false)
            adapter = object : BaseAdapter<ServiceHookBean>() {
                override fun bind(holder: ViewHolder, position: Int, data: ServiceHookBean) {
                    holder.itemView.findViewById<Button>(R.id.itemBtn).run {
                        text = data.serviceName

                        val service = this@SysActivity.getSystemService(data.serviceName)
                        if (service == null){
                            setBackgroundColor(Color.parseColor("#FF0000"))
                        } else {
                            var isProxy = false
                            service::class.java.let {
                                for (ii in it.interfaces) {
                                    if (ii == Proxy::class.java){
                                        isProxy = true
                                        break
                                    }
                                }
                            }
                            Log.e(this@SysActivity.tag, "is proxy ${isProxy}")
                            if (isProxy){
                                setBackgroundColor(Color.parseColor("#00FFFF"))
                            } else {
                                setBackgroundColor(Color.parseColor("#FF0000"))
                            }
                        }
                    }
                }

                override var layout: Int = R.layout.item_hook_list
            }.apply {
                this@apply.addData(SERVICE_HOOK_LIST)
            }
        }
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