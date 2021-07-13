package com.example.access.action

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.access.action.setting.AutoPermissionTask
import com.example.access.action.setting.BaseSettingTask
import com.example.access.bean.PermissionRuleBean
import com.example.access.utils.IntentUtils
import com.example.access.utils.PermissionUtils
import java.util.*

/**
 * @author lhr
 * @date 2021/7/6
 * @des 无障碍任务执行类
 */
object AccessibilityActionExecutor {
    private val tag = "AS_${this::class.java.simpleName}"
    private const val TASK_WAIT_TIME = 1000L
    private val mHandler = Handler(Looper.getMainLooper())
    private var isWaitTask = false
    private val actionQueue = ArrayDeque<PermissionRuleBean>()
    private var currentAction: BaseSettingTask? = null

    /**
     * 接收无障碍服务事件
     */
    fun acceptActionEvent(service: AccessibilityService, event: AccessibilityEvent) {
        if (currentAction != null) {
            currentAction?.acceptEvent(service, event)
            if (currentAction?.isFinish() == true) {
                currentAction = null
                isWaitTask = true
                mHandler.postDelayed({
                   startAction(service)
                    isWaitTask = false
                }, TASK_WAIT_TIME)
            }
        }
    }

    /**
     * 开始执行任务动作
     */
    fun startAction(service: AccessibilityService) {
        if (currentAction != null) {
            if (currentAction?.isFinish() == true
                || currentAction?.checkPermission(service.applicationContext) == true) {
                currentAction = null
            }else{
                currentAction?.requestPermission(service.applicationContext)
            }
        } else if (currentAction == null && actionQueue.isNotEmpty()) {
            val bean = actionQueue.remove()
            currentAction =
                AutoPermissionTask(bean, getRequestAction(service.applicationContext,bean), getCheckAction(bean))
        }
        currentAction?.requestPermission(service.applicationContext)
    }

    fun postAction(vararg action: PermissionRuleBean) {
        action.forEach {
            actionQueue.add(it)
        }
    }

    fun removeAction(action: PermissionRuleBean) {
        actionQueue.remove(action)
    }

    fun clearAction() {
        actionQueue.clear()
    }

    private fun getRequestAction(context: Context,bean: PermissionRuleBean): ((Context) -> Unit) {
        Log.e(tag, "getRequestAction:$bean")
        if (bean.ruleIntent != null){
            val intent = IntentUtils.getIntentByBean(context, bean.ruleIntent!!)
            Log.e(tag,"isIntentAvailable:" + IntentUtils.isIntentAvailable(context, intent))
            if (IntentUtils.isIntentAvailable(context, intent)){
                return {
                    it.startActivity(intent)
                }
            }
        }
        return when (bean.type) {
            1 -> PermissionUtils::gotoWindowEnableSetting
            2 -> PermissionUtils::gotoNotificationAccessSetting
            31 -> PermissionUtils::gotoAccessibilitySetting
            else -> { _ -> }
        }
    }

    private fun getCheckAction(bean: PermissionRuleBean): ((Context) -> Boolean) {
        return when (bean.type) {
            1 -> PermissionUtils::windowEnable
            2 -> PermissionUtils::notificationListenerEnable
            31 -> PermissionUtils::isSystemWriteEnable
            else -> { _ -> true }
        }
    }
}