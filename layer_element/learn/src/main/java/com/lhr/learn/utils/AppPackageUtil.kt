package com.lhr.learn.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.usage.StorageStatsManager
import android.content.*
import android.content.pm.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.*

/**
 * @CreateDate: 2022/6/28
 * @Author: mac
 * @Description: app软件安装信息获取
 */
object AppPackageUtil {
    private const val LOG_TAG = "AppPackageUtil"

    @SuppressLint("UnspecifiedImmutableFlag")
    @Suppress("DEPRECATION")
    fun uninstallApp(packageName: String, activity: Activity, resultCode: Int) {
        val uri = Uri.fromParts("package", packageName, null)

        val uninstallIntent = Intent(Intent.ACTION_UNINSTALL_PACKAGE, uri)
        activity.startActivityForResult(uninstallIntent, resultCode)
    }


    /**
     * 从系统中中创建packageInfo对象
     * @param context -
     * @param apkPath -
     * @param flags 解析标志
     */
    fun createPackageInfoSystem(
        context: Context,
        packageName: String,
        flags: Int = 0
    ): PackageInfo? {
        val packageManager = context.packageManager
        return packageManager.getPackageInfo(packageName, flags)
    }

    /**
     * 从apk中创建packageInfo对象
     * @param context -
     * @param apkPath -
     * @param flags 解析标志
     */
    fun createPackageInfoFromFile(
        context: Context,
        apkPath: String,
        flags: Int = 0
    ): PackageInfo? {
        val packageManager = context.packageManager
        return packageManager.getPackageArchiveInfo(apkPath, flags)?.apply {
            applicationInfo.sourceDir = apkPath
            applicationInfo.publicSourceDir = apkPath
        }
    }

    fun getAppInstallTime(info: PackageInfo): Long {
        return info.firstInstallTime
    }

    fun getAppIconUri(info: PackageInfo): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(info.packageName)
            .path(info.applicationInfo.icon.toString())
            .build()
    }

    /**
     * 当应用数据很大时该方法会很慢
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getApplicationSize(context: Context, info: PackageInfo): Long {
        //26以上的获取方法
        //调用前需要检查权限, 查询自己apk的磁盘占用不需要申请权限，查询非自己需要申请权限
        //非自己的包名
//        if (context.packageName != info.packageName) {
//            //此权限只能用户手动去设置里打开，故如有需要可以直接跳到设置界面
//            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            })
//        }
        val applicationInfo = info.applicationInfo
        var size = 0L
        val storageStatsManager =
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        try {
            val storageStats = storageStatsManager.queryStatsForUid(
                applicationInfo.storageUuid,
                applicationInfo.uid
            )
            size += storageStats.cacheBytes
            size += storageStats.dataBytes
            size += storageStats.appBytes
        } catch (e: Exception) {
        }
        return size
    }

    fun getApkLabel(context: Context, info: PackageInfo): String {
        val packageManager = context.packageManager
        return info.applicationInfo.loadLabel(packageManager).toString()
    }

    fun getApkIcon(context: Context, info: PackageInfo): Drawable? {
        val appInfo: ApplicationInfo = info.applicationInfo
        kotlin.runCatching {
            return appInfo.loadIcon(context.packageManager)
        }
        return null
    }

    fun getApkActivities(info: PackageInfo): Array<ActivityInfo> {
        return info.activities ?: emptyArray()
    }

    fun getApkProviders(info: PackageInfo): Array<ProviderInfo> {
        return info.providers
    }

    fun getApkReceivers(info: PackageInfo): Array<ActivityInfo> {
        return info.receivers
    }

    fun getApkServices(info: PackageInfo): Array<ServiceInfo> {
        return info.services
    }

    fun getApkPermissions(info: PackageInfo): Array<PermissionInfo> {
        return info.permissions
    }

}