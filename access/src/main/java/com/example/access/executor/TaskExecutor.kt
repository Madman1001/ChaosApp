package com.example.access.executor

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.example.access.executor.task.BaseTask
import java.util.*

/**
 * @author lhr
 * @date 2021/7/6
 * @des 无障碍任务执行类
 */
object TaskExecutor {
    private val tag = "AS_${this::class.java.simpleName}"
    private const val TASK_WAIT_TIME = 1000L
    private val mHandler = Handler(Looper.getMainLooper())
    private var isWaitTask = false
    private val actionQueue = ArrayDeque<BaseTask>()
    private var currentAction: BaseTask? = null

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
                   startExecuteTask(service)
                    isWaitTask = false
                }, TASK_WAIT_TIME)
            }
        }
    }

    /**
     * 开始执行动作任务
     */
    fun startExecuteTask(service: AccessibilityService) {
        if (isWaitTask){
            mHandler.removeCallbacksAndMessages(null)
            isWaitTask = false
        }
        if (currentAction != null) {
            if (currentAction?.isFinish() == true) {
                currentAction = null
            }else{
                currentAction?.startTask(service)
            }
        } else if (currentAction == null && actionQueue.isNotEmpty()) {
            currentAction = actionQueue.remove()

        }
        currentAction?.startTask(service)
    }

    /**
     * 停止执行动作任务
     */
    fun stopExecuteTask() {
        if (isWaitTask){
            mHandler.removeCallbacksAndMessages(null)
            isWaitTask = false
        }
        if (currentAction != null && currentAction?.isFinish() != true){
            currentAction?.stopTask()
            currentAction = null
        }
        clearTask()

    }

    fun postTask(task: BaseTask) {
        currentAction = task
    }

    fun removeAction(task: BaseTask) {
        if (currentAction == task){
            currentAction?.stopTask()
            currentAction = null
            if (isWaitTask){
                mHandler.removeCallbacksAndMessages(null)
                isWaitTask = false
            }
        }else{
            if (actionQueue.contains(task)) {
                actionQueue.remove(task)
            }
        }
    }

    fun clearTask() {
        actionQueue.clear()
    }
}