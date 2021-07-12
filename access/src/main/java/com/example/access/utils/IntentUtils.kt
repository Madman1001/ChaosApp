package com.example.access.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.example.access.bean.PermissionIntentBean

/**
 * @author lhr
 * @date 2021/7/12
 * @des 权限跳转工具
 */
object IntentUtils {
    private val tag = "AS_${this::class.java.simpleName}"
    fun getIntentByBean(context: Context,bean: PermissionIntentBean): Intent{
        Log.e(tag,bean.toString())
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (bean.permissionAction.isNotEmpty()){
            intent.action = bean.permissionAction
        }
        if (bean.permissionPackage.isNotEmpty() && bean.permissionActivity.isNotEmpty()){
            intent.component = ComponentName(bean.permissionPackage, bean.permissionActivity)
        }
        if (bean.permissionData.isNotEmpty()){
            intent.putExtra(bean.permissionData, context.packageName)
        }
        if (bean.permissionExtra.isNotEmpty()){
            intent.putExtra(bean.permissionExtra,context.packageName)
        }
        return intent
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        var available = false
        if (context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size > 0) {
            available = true
        }
        return available
    }
}