package com.example.access.factory

import android.accessibilityservice.AccessibilityService
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
        return uninstallTask(context,context.packageName)
    }

    fun uninstallTask(context: Context,packageName: String) : BaseTask {
        val task = TaskBean(0,"卸载自身程序")
        val action1 = ActionBean()
        action1.findTexts.addAll(arrayOf("卸载"))
        action1.behavior = "click"
        action1.needWaitWindow = true
        action1.notNeedBack = false
        val action2 = ActionBean()
        action2.findTexts.addAll(arrayOf("确定"))
        action2.behavior = "click"
        action2.needWaitWindow = true
        action2.notNeedBack = false
        task.actionList.add(action1)
        task.actionList.add(action2)
        val request:(Context)->Unit = {
            val intent = Intent()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", context.packageName, null)
            it.startActivity(intent)
        }
        val check:(Context)->Boolean = {false}
        return DefaultActionTask(task,request,check)
    }
}