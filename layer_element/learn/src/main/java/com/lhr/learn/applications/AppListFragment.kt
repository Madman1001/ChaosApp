package com.lhr.learn.applications

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.recyclerview.widget.LinearLayoutManager
import com.lhr.common.ui.BaseFragment
import com.lhr.learn.databinding.FragmentAppListBinding
import java.io.File


/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
class AppListFragment: BaseFragment<FragmentAppListBinding>(), AppInfoAdapter.ItemClickListener {

    private val packageManager: PackageManager by lazy { requireContext().packageManager }
    private var appList: ArrayList<AppInfo> = arrayListOf()
    private var appInfoAdapter: AppInfoAdapter = AppInfoAdapter(this)

    override fun initView(savedInstanceState: Bundle?) {
        mBinding.appListRv.run {
            layoutManager = LinearLayoutManager(this.context).apply {
                this.orientation = LinearLayoutManager.VERTICAL
            }
            appList.clear()
            appList.addAll(allApplications())
            appInfoAdapter.replaceData(appList)
            adapter = appInfoAdapter
        }
    }

    override fun appOpenClicked(position: Int) {
        val info = appList[position]
        uninstallApp(info)
    }

    private fun uninstallApp(vararg appInfos: AppInfo) {
        UninstallHelper.uninstallAllApp(requireActivity(), ArrayList<AppInfo>().apply {
            addAll(appInfos)
        })
    }

    private fun allApplications(): List<AppInfo>{
        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .map {
                val hasNativeLibs = if (it.nativeLibraryDir != null) {
                    val fileDir = File(it.nativeLibraryDir)
                    val list = fileDir.listFiles()
                    list != null && list.isNotEmpty()
                } else false
                AppInfo(it.loadLabel(packageManager).toString(), it.packageName,
                    it.uid, it.sourceDir, it.nativeLibraryDir, hasNativeLibs,
                    getAppIconUri(it.packageName),
                    getApplicationSize(requireContext(), it))
            }
    }

    private fun getAppIconUri(packageName: String): Uri {
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(packageName)
            .path(getResourceId(packageName).toString())
            .build()
    }

    private fun getResourceId(packageName: String): Int {
        val packageInfo: PackageInfo
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            return 0
        }
        return packageInfo.applicationInfo.icon
    }

    private fun getApplicationSize(context: Context, info: ApplicationInfo): Long{
        //26以上的获取方法
        //调用前需要检查权限, 查询自己apk的磁盘占用不需要申请权限，查询非自己需要申请权限
        //非自己的包名
        if (!hasUsageStatsPermission(context)) {
            //检查权限(查询自己apk的磁盘占用不需要申请权限，查询非自己需要申请权限)
            //此权限只能用户手动去设置里打开，故如有需要可以直接跳到设置界面
                try {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            return 0L
        }
        var size = 0L
        val storageStatsManager =
            context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        try {
            val ai = context.packageManager.getApplicationInfo(info.packageName, 0)
            val storageStats = storageStatsManager.queryStatsForUid(ai.storageUuid, info.uid)
            size += storageStats.cacheBytes
            size += storageStats.dataBytes
            size += storageStats.appBytes
        } catch (e: Exception) {
        }
        return size
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        var granted = false
        granted =
            if (mode == AppOpsManager.MODE_DEFAULT)
                context.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
            else mode == AppOpsManager.MODE_ALLOWED
        return granted
    }
}