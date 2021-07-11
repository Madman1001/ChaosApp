package com.example.access.action.setting

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.access.bean.PermissionRuleBean
import com.example.access.utils.AccessibilityUtils
import com.example.access.utils.PermissionUtils

/**
 * @author lhr
 * @date 2021/7/8
 * @des 自动授权动作
 */
class AutoPermissionTask(private val bean: PermissionRuleBean): BaseSettingTask() {
    private val tag = this::class.java.simpleName
    /**
     * 请求权限
     */
    private val requestPermissionAction = PermissionUtils::gotoNotificationAccessSetting

    /**
     * 检查权限
     */
    private val checkPermissionAction = PermissionUtils::notificationListenerEnable

    /**
     * 任务集
     */
    private val actions = bean.getRuleActions()

    private val ruleIntent = bean.ruleIntent

    private var currentIndex = -1
    init {
        actionStatus = ActionStatus.PREPARED
    }

    override fun acceptEvent(service: AccessibilityService, event: AccessibilityEvent) {
        if (isExecuting() && enableAcceptEvent(event)) {
            if (currentIndex >= 0 && currentIndex < actions.size) {
                val action = actions[currentIndex]
                var node:AccessibilityNodeInfo? = null
                for (text in action.findTexts){
                    node = AccessibilityUtils.findViewByName(service,text)
                    if (node != null){
                        break
                    }
                }
                if (node != null) {
                    if (action.behavior == "click") {
                        if (AccessibilityUtils.clickView(service, node)) {
                            currentIndex++
                            if (action.needWaitWindow) {
                                actionStatus = ActionStatus.WAIT_WINDOW
                            }
                        }
                    }
                }else{
                    //未找到目标，滑动视图进行查找
                    if (AccessibilityUtils.scrollForwardView(service,action.clickNode)
                        || AccessibilityUtils.scrollBackwardView(service,action.clickNode)){
                        actionStatus = ActionStatus.WAIT_SCROLL
                    }else {
                        actionStatus = ActionStatus.FAIL
                    }
                }

                if (checkPermission(service.applicationContext)){
                    currentIndex = -1
                    actionStatus = ActionStatus.SUCCESS
                }
                Log.e(tag,"执行动作：$action 任务状态：${this.actionStatus}")
            }
        }
    }

    override fun requestPermission(context: Context) {
        if (!checkPermissionAction(context)) {
            //未申请权限，任务开始
            requestPermissionAction(context)
            actionStatus = ActionStatus.WAIT_FOCUSED
            currentIndex = 0
        } else {
            //权限已经完成，任务结束
            actionStatus = ActionStatus.FAIL
        }
    }

    override fun checkPermission(context: Context): Boolean {
        return checkPermissionAction(context)
    }
}