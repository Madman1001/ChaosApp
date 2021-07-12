package com.example.access.action.setting

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.access.bean.PermissionActionBean
import com.example.access.bean.PermissionRuleBean
import com.example.access.utils.AccessibilityUtils

/**
 * @author lhr
 * @date 2021/7/8
 * @des 自动授权动作
 */
class AutoPermissionTask(
    private val bean: PermissionRuleBean,
    requestAction: (Context) -> Unit,
    checkAction: (Context) -> Boolean
) : BaseSettingTask() {

    private val mHandler = Handler(Looper.getMainLooper())

    /**
     * 请求权限动作
     */
    private val requestPermissionAction = requestAction

    /**
     * 检查权限动作
     */
    private val checkPermissionAction = checkAction

    /**
     * 任务集
     */
    private val actions = bean.getRuleActions()

    private val ruleIntent = bean.ruleIntent

    private var currentIndex = -1

    private var taskWait = false

    init {
        taskStatus = TaskStatus.PREPARED
    }

    override fun onAcceptEvent(service: AccessibilityService, event: AccessibilityEvent) {
        if (!taskWait) {
            Log.e(tag, "executing currentIndex $currentIndex")
            if (currentIndex >= 0 && currentIndex < actions.size) {
                val action = actions[currentIndex]
                Log.e(tag, "executing action $action")
                //检查动作是否完成
                if (checkPermission(service.applicationContext)) {
                    currentIndex = -1
                    taskStatus = TaskStatus.SUCCESS
                }else{
                    taskWait = true
                    mHandler.postDelayed({
                        execAction(action, service)
                        taskWait = false
                    },action.needWaitTime * 1L + 300)
                }
            }else{
                if (currentIndex != -1) {
                    currentIndex = -1
                    if (actions.isNotEmpty() && actions[actions.size - 1].checkNode.isNotEmpty()) {
                        val checkNode: AccessibilityNodeInfo? =
                            AccessibilityUtils.findViewByClassName(
                                service,
                                actions[actions.size - 1].checkNode
                            )
                        if (checkNode?.isChecked == actions[actions.size - 1].checkStatus) {
                            taskStatus = TaskStatus.SUCCESS
                        }else{
                            taskStatus = TaskStatus.FAIL
                        }
                    } else {
                        currentIndex = -1
                        taskStatus = TaskStatus.SUCCESS
                    }
                }
            }
            Log.e(tag, "权限类型：${bean.type} 权限状态：${taskStatus}")
        }
    }

    /**
     * 执行动作
     */
    private fun execAction(action: PermissionActionBean,service: AccessibilityService){
        var node: AccessibilityNodeInfo? = null
        for (text in action.findTexts) {
            Log.e(tag, "find node $text")
            node = AccessibilityUtils.findViewByName(service, text)
            if (node != null) {
                break
            }
        }
        if (node != null) {
            //点击目标
            if (action.behavior == "click") {
                actionFlag = ActionFlag.WAIT_CLICK
                if (action.checkNode.isNotEmpty()){
                    actionFlag = actionFlag or ActionFlag.WAIT_CONTENT_CHANGED
                }
                if (!AccessibilityUtils.clickView(service, node)) {
                    taskStatus = TaskStatus.FAIL
                }else{
                    currentIndex++
                }
                Log.e(tag, "执行动作：$action 任务状态：${this.taskStatus}")
            }
        } else {
            //未找到目标，滑动视图进行查找
            actionFlag = ActionFlag.WAIT_SCROLL or ActionFlag.WAIT_CONTENT_CHANGED
            if (!AccessibilityUtils.scrollForwardView(service, action.clickNode)
                && !AccessibilityUtils.scrollBackwardView(service, action.clickNode)
            ) {
                taskStatus = TaskStatus.FAIL
            }
        }
    }

    override fun requestPermission(context: Context) {
        if (!checkPermissionAction(context)) {
            taskStatus = TaskStatus.EXECUTING
            actionFlag = ActionFlag.WAIT_WINDOW or ActionFlag.WAIT_CONTENT_CHANGED
            currentIndex = 0
            //未获取权限，开始任务
            requestPermissionAction(context)
        } else {
            //权限已获取，结束任务
            taskStatus = TaskStatus.FAIL
        }
    }

    override fun checkPermission(context: Context): Boolean {
        return checkPermissionAction(context)
    }
}