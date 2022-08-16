package com.lhr.learn.applications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lhr.learn.utils.AppPackageUtil

/**
 * @CreateDate: 2022/6/29
 * @Author: mac
 * @Description: 软件卸载透明activity
 */
class UninstallHelper: AppCompatActivity() {
    private val uninstallList: ArrayList<AppInfo> = ArrayList()
    private val resultReceiver: AppPackageReceiver by lazy { AppPackageReceiver(this::onPackageChangeReceiver) }

    //正在卸载的包名
    private var appInfo: AppInfo? = null
    private var uninstallPackageName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.overridePendingTransition(0, 0)

        uninstallList.clear()

        val list = this.intent.getParcelableArrayListExtra<AppInfo>(KEY_UNINSTALL_LIST)
        if (list == null || list.isEmpty()){
            finish()
            return
        }
        uninstallList.addAll(list)
        resultReceiver.register(this)

        internalUninstallSelectPackage()
    }

    override fun onDestroy() {
        super.onDestroy()
        resultReceiver.unregister()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UNINSTALL_RESULT_CODE ){
            if (appInfo == null){
                return
            }
            if (resultCode != Activity.RESULT_OK){
                //取消卸载
                internalUninstallSelectPackage()
            } else {
                //确认卸载
                uninstallPackageName = appInfo?.packageName ?: ""
            }
        }
    }

    private fun internalUninstallSelectPackage(){
        if (uninstallList.isEmpty()) {
            finish()
            return
        }
        val info = uninstallList.removeAt(0)
        appInfo = info
        AppPackageUtil.uninstallApp(info.packageName, this, UNINSTALL_RESULT_CODE)
    }

    private fun onPackageChangeReceiver(context: Context?, intent: Intent?){
        val packageName = intent?.data?.schemeSpecificPart ?: ""
        when(intent?.action){
            Intent.ACTION_PACKAGE_REMOVED -> {
                if (packageName == uninstallPackageName){
                    //卸载成功
                    uninstallPackageName = ""
                    appInfo = null
                    internalUninstallSelectPackage()
                }
            }
        }
    }

    companion object{
        private const val KEY_UNINSTALL_LIST = "KEY_UNINSTALL_LIST"
        private const val UNINSTALL_RESULT_CODE = 1008
        fun uninstallAllApp(context: Context, list: ArrayList<AppInfo>){
            val intent = Intent(context, UninstallHelper::class.java)
            intent.putParcelableArrayListExtra(KEY_UNINSTALL_LIST, list)
            if (context !is Activity){
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            } else {
                context.overridePendingTransition(0, 0)
            }
            context.startActivity(intent)
        }
    }
}