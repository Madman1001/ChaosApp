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
    var actionStatus = ActionStatus.NONE
        protected set

    abstract fun acceptEvent(service: AccessibilityService, event: AccessibilityEvent)

    fun isExecuting(): Boolean {
        return when (actionStatus) {
            ActionStatus.WAIT_WINDOW,
            ActionStatus.WAIT_FOCUSED,
            ActionStatus.WAIT_SCROLL,
            ActionStatus.WAIT_BACK -> true
            else -> false
        }
    }

    fun isFinish(): Boolean {
        return when (actionStatus) {
            ActionStatus.SUCCESS,
            ActionStatus.FAIL -> true
            else -> false
        }
    }

    fun enableAcceptEvent(event: AccessibilityEvent): Boolean{
        var enable = false
        when(actionStatus){
            ActionStatus.WAIT_WINDOW ->{
                //等待窗体事件
                if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
                    enable = true
                }
            }
            ActionStatus.WAIT_SCROLL ->{
                //等待滚动事件
                if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED){
                    enable = true
                }
            }

            ActionStatus.WAIT_CLICK ->{
                //等待点击事件
                if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED){
                    enable = true
                }
            }

            ActionStatus.WAIT_FOCUSED ->{
                //等待焦点事件
                if (event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED){
                    enable = true
                }
            }
        }

        return enable
    }

    enum class ActionStatus {
        NONE,
        PREPARED,//准备
        WAIT_FOCUSED,//等待界面切换
        WAIT_CLICK,//等待点击
        WAIT_SCROLL,//等待滚动
        WAIT_WINDOW,//等待弹窗
        WAIT_BACK,//等待返回
        SUCCESS,//完成
        FAIL//失败
    }
}