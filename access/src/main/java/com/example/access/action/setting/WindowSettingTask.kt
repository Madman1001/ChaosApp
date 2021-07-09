package com.example.access.action.setting

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import com.example.access.utils.AccessibilityUtils
import com.example.access.utils.PermissionUtils
import java.util.*

/**
 * @author lhr
 * @date 2021/7/8
 * @des 弹窗授权动作
 */
class WindowSettingTask : BaseSettingTask() {
    private val runnableQueue =
        ArrayDeque<((AccessibilityService, AccessibilityEvent) -> Boolean)>()
    private var currentRunnable: ((AccessibilityService, AccessibilityEvent) -> Boolean)? = null

    init {
        actionStatus = ActionStatus.PREPARED

        runnableQueue.add { service, event ->
            val appName = "My Application"
            if (AccessibilityUtils.clickViewByName(service, appName)) {
                actionStatus = ActionStatus.WAIT_WINDOW
                true
            } else {
                if (!AccessibilityUtils.scrollBackwardView(service)) {
                    AccessibilityUtils.scrollForwardView(service)
                }
                actionStatus = ActionStatus.WAIT_SCROLL
                false
            }
        }

        runnableQueue.add { service, event ->
            val target = "允许显示在其他应用的上层"
            if (AccessibilityUtils.clickViewByName(service, target)) {
                if (PermissionUtils.windowEnable(service.applicationContext)) {
                    actionStatus = ActionStatus.SUCCESS
                    true
                } else {
                    actionStatus = ActionStatus.FAIL
                    true
                }
            } else {
                false
            }
        }
    }

    override fun acceptEvent(service: AccessibilityService, event: AccessibilityEvent) {
        if (isExecuting()) {
            if (enableAcceptEvent(event)) {
                if (currentRunnable == null) {
                    if (runnableQueue.isNotEmpty()) {
                        currentRunnable = runnableQueue.remove()
                    }
                }

                if (currentRunnable?.invoke(service, event) == true) {
                    currentRunnable = null
                }
            }
        }
    }

    override fun requestPermission(context: Context) {
        if (!checkPermission(context)) {
            //未申请权限，任务开始
            PermissionUtils.gotoWindowEnableSetting(context)
            actionStatus = ActionStatus.WAIT_WINDOW
        } else {
            //权限已经完成，任务结束
            actionStatus = ActionStatus.FAIL
        }
    }

    override fun checkPermission(context: Context): Boolean {
        return PermissionUtils.windowEnable(context)
    }
}