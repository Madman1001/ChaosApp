package com.example.access.action

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.access.action.setting.BaseSettingTask
import java.util.*

/**
 * @author lhr
 * @date 2021/7/6
 * @des
 */
object AccessibilityActionExecutor {
    private val actionQueue = ArrayDeque<BaseSettingTask>()
    private var currentAction:BaseSettingTask? = null

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
            currentAction = actionQueue.remove()
        }
        if (currentAction?.isExecuting() != true && currentAction?.isFinish() != true){
            currentAction?.requestPermission(service.applicationContext)
        }
    }

    fun postAction(vararg action: BaseSettingTask){
        action.forEach {
            actionQueue.add(it)
        }
    }

    fun removeAction(action: BaseSettingTask){
        actionQueue.remove(action)
    }

    fun clearAction(){
        actionQueue.clear()
    }

}