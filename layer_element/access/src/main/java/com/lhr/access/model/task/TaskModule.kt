package com.lhr.access.model.task

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.lhr.access.bean.ActionBean
import com.lhr.access.bean.TaskBean
import com.lhr.access.executor.TaskExecutor
import com.lhr.access.model.IHandleModule

/**
 * @CreateDate: 2022/4/25
 * @Author: mac
 * @Description: 任务组件
 */
class TaskModule: IHandleModule{
    private lateinit var task: TaskBean
    private lateinit var parent: IHandleModule

    private val targetPackage = "com.lhr.chaos"
    override fun attachHandleModel(handle: IHandleModule) {
        parent = handle
        task = TaskBean(0, "华为跳过安装检测").apply {
            val action1 = ActionBean.ActionBuild().let {
                it.findTexts.addAll(arrayOf("在线识别潜在风险"))
                it.behavior = "click"
                it.needWaitTime = 100
                it.build()
            }
            val action2 = ActionBean.ActionBuild().let {
                it.findTexts.addAll(arrayOf("取消"))
                it.behavior = "click"
                it.needWaitTime = 300
                it.build()
            }
            this.actionList.add(action1)
            this.actionList.add(action2)
        }

    }

    override fun onHandle(event: AccessibilityEvent) {
        if (event.packageName == targetPackage
            && parent is AccessibilityService){
            TaskExecutor.acceptActionEvent(parent as AccessibilityService, event)
        }
    }

    override fun detachHandleModel(handle: IHandleModule) {

    }
}