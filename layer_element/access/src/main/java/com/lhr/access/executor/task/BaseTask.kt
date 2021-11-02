package com.lhr.access.executor.task

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import com.lhr.access.executor.ITask
import com.lhr.access.executor.ITask.TaskStatus
import java.util.*

/**
 * @author lhr
 * @date 2021/7/8
 * @des 设置权限动作基类
 */
abstract class BaseTask(val taskType: Int, val taskName: String):ITask {
    protected val tag = "AS_${this::class.java.simpleName}"

    /**
     * 任务状态
     */
    var taskStatus = TaskStatus.NONE
        protected set

    /**
     * 动作执行所需的动作标志，接收到事件时使用，需要执行顺序
     */
    private val actionFlagLink = LinkedList<Int>()

    /**
     * 处理事件接收
     */
    fun acceptEvent(service: AccessibilityService, event: AccessibilityEvent) {
        if (isExecuting() && enableAcceptEvent(event)) {
            onAcceptEvent(service, event)
        }
    }

    /**
     * 检查任务状态
     */
    abstract fun checkTask(context: Context): Boolean

    /**
     * 事件接收处理类
     */
    abstract fun onAcceptEvent(service: AccessibilityService, event: AccessibilityEvent)

    /**
     * 判断任务是否正在执行
     */
    fun isExecuting(): Boolean {
        return when (taskStatus) {
            TaskStatus.EXECUTING -> true
            else -> false
        }
    }

    /**
     * 判断任务是否完成
     */
    fun isFinish(): Boolean {
        return when (taskStatus) {
            TaskStatus.SUCCESS,
            TaskStatus.FAIL -> true
            else -> false
        }
    }

    /**
     * 判断是否可以接收事件，如果事件和目标相符，则将目标flag移除队列
     */
    protected open fun enableAcceptEvent(event: AccessibilityEvent): Boolean {
        if (actionFlagLink.isEmpty()) {
            //动作标记为空，不执行动作
            return false
        }

        val actionFlag: Int = actionFlagLink.first

        if (actionFlag == ActionFlag.WAIT_ALL){
            actionFlagLink.remove()
            return true
        }

        val flag =
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    //等待窗体事件
                    ActionFlag.WAIT_WINDOW
                }
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                    //等待窗体内容事件
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

        if (actionFlag and flag != 0) {
            actionFlagLink.remove()
            return actionFlagLink.isEmpty()
        }

        //等待滚动内容填充
        if (actionFlag and ActionFlag.WAIT_CONTENT_SCROLL != 0) {
            if ((flag == ActionFlag.WAIT_SCROLL && event.itemCount > 0 && event.toIndex > 0)
                || (flag == ActionFlag.WAIT_CONTENT_CHANGED && event.itemCount > 0)
            ) {
                actionFlagLink.remove()
                return actionFlagLink.isEmpty()
            }
        }

        //等待全屏窗体事件
        if (actionFlag and ActionFlag.WAIT_FULL_WINDOW != 0) {
            if (flag == ActionFlag.WAIT_WINDOW && event.isFullScreen) {
                actionFlagLink.remove()
                return actionFlagLink.isEmpty()
            }
        }

        return false
    }

    protected fun setActionFlag(vararg flags: Int){
        clearActionFlag()
        for (flag in flags) {
            actionFlagLink.add(flag)
        }
    }

    protected fun addActionFlag(vararg flags: Int) {
        for (flag in flags) {
            actionFlagLink.add(flag)
        }
    }

    protected fun hasActionFlag(actionFlag: Int) : Boolean{
        for (flag in actionFlagLink) {
            if (flag and actionFlag != 0){
                return true
            }
        }
        return false
    }

    protected fun getActionFlagSize(): Int{
        return actionFlagLink.size
    }

    protected fun clearActionFlag() {
        actionFlagLink.clear()
    }

    override fun toString(): String {
        var actions = ""
        for (flag in actionFlagLink){
            actions += "${Integer.toHexString(flag)}, "
        }
        return "BaseTask(taskType=$taskType, taskName='$taskName', tag='$tag', taskStatus=$taskStatus, actionFlagLink=[$actions])"
    }

    /**
     * 执行动作所需的标志
     */
    object ActionFlag {
        val WAIT_ALL = -0x1//等待任意事件
        val WAIT_FOCUSED = 0x00000002//等待界面切换事件
        val WAIT_CLICK = 0x00000004//等待点击事件
        val WAIT_SCROLL = 0x00000008//等待滚动事件
        val WAIT_WINDOW = 0x00000020//等待窗体事件
        val WAIT_CONTENT_CHANGED = 0x00000040//等待内容发送改变事件
        val WAIT_BACK = 0x00000080//等待返回事件
        val WAIT_CONTENT_SCROLL = 0x00000200//等待滚动内容填充
        val WAIT_FULL_WINDOW = 0x00000400//等待全屏窗体事件
    }
}