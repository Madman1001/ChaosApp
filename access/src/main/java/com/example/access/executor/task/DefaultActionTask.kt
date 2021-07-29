package com.example.access.executor.task

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.access.bean.ActionBean
import com.example.access.bean.TaskBean
import com.example.access.utils.AccessibilityUtils
import com.example.access.executor.ITask.*

/**
 * @author lhr
 * @date 2021/7/8
 * @des 自动授权动作
 */
class DefaultActionTask(
    private val bean: TaskBean,
    private val startAction: (Context) -> Unit
) : BaseTask(bean.type,bean.name) {

    private val mHandler = Handler(Looper.getMainLooper())

    /**
     * 任务集
     */
    private val actions = bean.actionList

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
                if (checkTask(service.applicationContext)) {
                    currentIndex = -1
                    taskStatus = TaskStatus.SUCCESS
                }else{
                    taskWait = true
                    mHandler.postDelayed({
                        execAction(action, service)
                        taskWait = false
                    },action.needWaitTime * 1L)
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
    private fun execAction(action: ActionBean, service: AccessibilityService){
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
                setActionFlag(ActionFlag.WAIT_CLICK)
                if (action.checkNode.isNotEmpty()){
                    addActionFlag(ActionFlag.WAIT_CONTENT_CHANGED)
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
            setActionFlag(ActionFlag.WAIT_SCROLL or ActionFlag.WAIT_CONTENT_CHANGED)
            if (!AccessibilityUtils.scrollForwardView(service, action.actionNode)
                && !AccessibilityUtils.scrollBackwardView(service, action.actionNode)
            ) {
                taskStatus = TaskStatus.FAIL
            }
        }
    }

    override fun startTask(context: Context) {
        if (!checkTask(context)) {
            taskStatus = TaskStatus.EXECUTING
            setActionFlag(ActionFlag.WAIT_WINDOW , ActionFlag.WAIT_CONTENT_CHANGED)
            currentIndex = 0
            taskWait = false
            //未获取权限，开始任务
            startAction(context)
        } else {
            //权限已获取，结束任务
            taskStatus = TaskStatus.FAIL
        }
    }

    override fun checkTask(context: Context): Boolean {
        return false
    }

    override fun stopTask() {
        taskStatus = TaskStatus.FAIL
        currentIndex = -1
        taskWait = false
    }
}