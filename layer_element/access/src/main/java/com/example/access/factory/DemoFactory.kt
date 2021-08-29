package com.example.access.factory

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.example.access.executor.task.DefaultActionTask
import com.example.access.executor.task.BaseTask
import com.example.access.bean.ActionBean
import com.example.access.bean.TaskBean

/**
 * @author lhr
 * @date 2021/7/29
 * @des
 */
object DemoFactory {

    /**
     * 卸载任务
     */
    fun uninstallSelfTask(context: Context) : BaseTask {
        return uninstallTask(context.packageName)
    }

    fun uninstallTask(packageName: String) : BaseTask {
        val task = TaskBean(0,"卸载自身程序")
        val action1 = ActionBean.ActionBuild().let {
            it.findTexts.addAll(arrayOf("卸载","Uninstall"))
            it.behavior = "click"
            it.needWaitTime = 1500
            it.build()
        }

        val action2 = ActionBean.ActionBuild().let {
            it.findTexts.addAll(arrayOf("确定","OK"))
            it.behavior = "click"
            it.needWaitTime = 1500
            it.build()
        }
        task.actionList.add(action1)
        task.actionList.add(action2)
        val request:(Context)->Unit = {
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", packageName, null)
            it.startActivity(intent)
        }
        return DefaultActionTask(task,request)
    }
}