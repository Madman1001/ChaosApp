package com.example.access.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log

/**
 * @author lhr
 * @date 2021/7/12
 * @des 权限跳转工具
 */
object IntentUtils {
    private val tag = "AS_${this::class.java.simpleName}"

    @SuppressLint("QueryPermissionsNeeded")
    fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        var available = false
        if (context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size > 0) {
            available = true
        }
        return available
    }
}