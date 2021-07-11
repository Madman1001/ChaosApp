package com.example.access.action

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.access.action.setting.AutoPermissionTask
import com.example.access.action.setting.BaseSettingTask
import com.example.access.bean.PermissionRuleBean
import java.util.*

/**
 * @author lhr
 * @date 2021/7/6
 * @des
 */
object AccessibilityActionExecutor {
    private val actionQueue = ArrayDeque<PermissionRuleBean>()
    private var currentAction:AutoPermissionTask? = null

    fun acceptActionEvent(service: AccessibilityService, event: AccessibilityEvent){
        if (currentAction != null){
            currentAction?.acceptEvent(service, event)
        }
        if (currentAction?.isFinish() == true){
            currentAction = null
            startAction(service)
        }
    }

    fun startAction(service: AccessibilityService){
        if (currentAction == null && actionQueue.isNotEmpty()){
            currentAction = AutoPermissionTask(actionQueue.remove())
        }
        if (currentAction?.isExecuting() != true && currentAction?.isFinish() != true){
            currentAction?.requestPermission(service.applicationContext)
        }
    }

    fun postAction(vararg action: PermissionRuleBean){
        action.forEach {
            actionQueue.add(it)
        }
    }

    fun removeAction(action: PermissionRuleBean){
        actionQueue.remove(action)
    }

    fun clearAction(){
        actionQueue.clear()
    }

}