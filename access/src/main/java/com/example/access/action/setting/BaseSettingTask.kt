package com.example.access.action.setting

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.access.action.IPermissionTask

/**
 * @author lhr
 * @date 2021/7/8
 * @des 设置权限动作基类
 */
abstract class BaseSettingTask : IPermissionTask {
    protected val tag = "AS_${this::class.java.simpleName}"

    var taskStatus = TaskStatus.NONE
        protected set

    var actionFlag: Int = 0
        protected set

    fun acceptEvent(service: AccessibilityService, event: AccessibilityEvent) {
        if (isExecuting() && enableAcceptEvent(event)) {
            onAcceptEvent(service, event)
        }
    }

    abstract fun onAcceptEvent(service: AccessibilityService, event: AccessibilityEvent)

    fun isExecuting(): Boolean {
        return when (taskStatus) {
            TaskStatus.EXECUTING -> true
            else -> false
        }
    }

    fun isFinish(): Boolean {
        return when (taskStatus) {
            TaskStatus.SUCCESS,
            TaskStatus.FAIL -> true
            else -> false
        }
    }

    fun enableAcceptEvent(event: AccessibilityEvent): Boolean {
        val flag =
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    //等待窗体事件
                    ActionFlag.WAIT_WINDOW
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    //等待窗体事件
                    ActionFlag.WAIT_CONTENT_CHANGED
                }
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                    //等待滚动事件
                    ActionFlag.WAIT_SCROLL
                }

                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    //等待点击事件
                    ActionFlag.WAIT_CLICK
                }

                AccessibilityEvent.TYPE_VIEW_FOCUSED -> {
                    //等待焦点事件
                    ActionFlag.WAIT_FOCUSED
                }
                else -> {
                    return false
                }
            }
        val isInclude = actionFlag and flag != 0
        if (isInclude) {
            actionFlag = actionFlag and flag.inv()
            return actionFlag == 0
        }
        return false
    }

    /**
     * 任务状态
     */
    enum class TaskStatus {
        NONE,
        PREPARED,//准备
        EXECUTING,//准备
        SUCCESS,//完成
        FAIL//失败
    }

    /**
     * 执行动作所需的标志
     */
    object ActionFlag {
        val WAIT_FOCUSED = 0x00000002//等待界面切换
        val WAIT_CLICK = 0x00000004//等待点击
        val WAIT_SCROLL = 0x00000008//等待滚动
        val WAIT_WINDOW = 0x00000020//等待窗体
        val WAIT_CONTENT_CHANGED = 0x00000040//等待内容发送改变
        val WAIT_BACK = 0x00000080//等待返回
    }
}