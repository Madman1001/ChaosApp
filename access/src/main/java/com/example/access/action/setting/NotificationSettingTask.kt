package com.example.access.action.setting

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import com.example.access.AccessibilityUtils
import com.example.access.utils.PermissionUtils

/**
 * @author lhr
 * @date 2021/7/8
 * @des 通知管理授权动作
 */
class NotificationSettingTask: BaseSettingTask() {
    init {
        actionStatus = ActionStatus.PREPARED
    }

    override fun acceptEvent(service: AccessibilityService, event: AccessibilityEvent) {
        if (actionStatus == ActionStatus.WAIT_FOCUSED && event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            if (AccessibilityUtils.clickViewByName(service, "允许出现在其他应用上")) {
                if (checkPermission(service.applicationContext)) {
                    actionStatus = ActionStatus.SUCCESS
                } else {
                    actionStatus = ActionStatus.FAIL
                }
                AccessibilityUtils.backAction(service)
            }
        }
    }

    override fun requestPermission(context: Context) {
        if (!checkPermission(context)) {
            //未申请权限，任务开始
            PermissionUtils.gotoNotificationAccessSetting(context)
            actionStatus = ActionStatus.WAIT_FOCUSED
        } else {
            //权限已经完成，任务结束
            actionStatus = ActionStatus.FAIL
        }
    }

    override fun checkPermission(context: Context): Boolean {
        return PermissionUtils.notificationListenerEnable(context)
    }
}