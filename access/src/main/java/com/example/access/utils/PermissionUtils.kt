package com.example.access.utils

import android.app.Activity
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object PermissionUtils {
    /**
     * 检测是否有某个权限
     *
     * @param context
     * @param permission
     * @return
     */
    fun hasSelfPermission(
        context: Context?,
        permission: String?
    ): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            permission!!
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(
        activity: Activity?,
        permissions: Array<String?>?,
        permissionCode: Int
    ) {
        ActivityCompat.requestPermissions(activity!!, permissions!!, permissionCode)
    }

    fun shouldShowRequestPermissionRationale(
        activity: Activity?,
        permission: String?
    ): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity!!,
            permission!!
        )
    }

    /**
     * 监听有没有通知栏访问权限
     *
     * @return
     */
    fun notificationListenerEnable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return true
        }
        val packageNames =
            NotificationManagerCompat.getEnabledListenerPackages(context)
        return packageNames.contains(context.packageName)
    }

    /**
     * 打开系统的访问权限页面
     */
    fun gotoNotificationAccessSetting(context: Context) {
        try {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            if (context is Activity) {
                context.startActivityForResult(intent, 1024)
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: ActivityNotFoundException) {
            try {
                val intent = Intent()
                val cn = ComponentName(
                    "com.android.settings",
                    "com.android.settings.Settings\$NotificationAccessSettingsActivity"
                )
                intent.component = cn
                intent.putExtra(":settings:show_fragment", "NotificationAccessSettings")
                if (context is Activity) {
                    context.startActivityForResult(intent, 1024)
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    /**
     * 判断有没有windowmanager权限  6.0以上才去获取
     *
     * @param context
     * @return
     */
    fun windowEnable(context: Context?): Boolean {
        var isWindowEnable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isWindowEnable = Settings.canDrawOverlays(context)
        }
        return isWindowEnable
    }

    fun gotoWindowEnableSetting(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName())
            )
            if (context is Activity) {
                context.startActivityForResult(intent, 1024)
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }

    /**
     * 判断铃声控制权限  6.0以上才去获取
     *
     * @param context
     * @return
     */
    fun ringtoneAdjustEnable(context: Context): Boolean {
        var isNotificationPolicyEnable = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            isNotificationPolicyEnable = mNotificationManager.isNotificationPolicyAccessGranted
        }
        return isNotificationPolicyEnable
    }

    fun gotoRingtoneAdjustEnableSetting(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                val intent =
                    Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                if (context is Activity) {
                    context.startActivityForResult(intent, 1024)
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }
        }
    }

    /**
     * 是否全部都给了权限
     *
     * @param grantResults
     * @return
     */
    fun isGranted(grantResults: IntArray): Boolean {
        if (grantResults.isNotEmpty()) {
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }
}