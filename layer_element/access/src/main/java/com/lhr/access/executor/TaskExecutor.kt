package com.lhr.access.executor

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.lhr.access.executor.task.BaseTask
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
    private val taskQueue = ArrayDeque<BaseTask>()
    private var currentTask: BaseTask? = null

    /**
     * 接收无障碍服务事件
     */
    fun acceptActionEvent(service: AccessibilityService, event: AccessibilityEvent) {
        if (currentTask != null) {
            currentTask?.acceptEvent(service, event)
            if (currentTask?.isFinish() == true) {
                currentTask = null
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
        if (currentTask != null) {
            if (currentTask?.isFinish() == true) {
                currentTask = null
            }else{
                currentTask?.startTask(service)
            }
        } else if (currentTask == null && taskQueue.isNotEmpty()) {
            currentTask = taskQueue.remove()

        }
        currentTask?.startTask(service)
    }

    /**
     * 停止执行动作任务
     */
    fun stopExecuteTask() {
        if (isWaitTask){
            mHandler.removeCallbacksAndMessages(null)
            isWaitTask = false
        }
        if (currentTask != null && currentTask?.isFinish() != true){
            currentTask?.stopTask()
            currentTask = null
        }
        clearTask()

    }

    fun postTask(vararg task: BaseTask) {
        taskQueue.addAll(task)
    }

    fun removeAction(task: BaseTask) {
        if (currentTask == task){
            currentTask?.stopTask()
            currentTask = null
            if (isWaitTask){
                mHandler.removeCallbacksAndMessages(null)
                isWaitTask = false
            }
        }else{
            if (taskQueue.contains(task)) {
                taskQueue.remove(task)
            }
        }
    }

    fun clearTask() {
        taskQueue.clear()
    }
}