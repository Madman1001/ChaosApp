package com.lhr.access.executor

import android.content.Context

/**
 * @author lhr
 * @date 2021/7/29
 * @des
 */
interface ITask {

    /**
     * 开始执行任务
     */
    fun startTask(context: Context)

    /**
     * 停止执行任务
     */
    fun stopTask()

    /**
     * 任务状态
     */
    enum class TaskStatus {
        NONE,
        PREPARED,//准备
        EXECUTING,//准备
        SUCCESS,//完成
        FAIL//失败
    }
}