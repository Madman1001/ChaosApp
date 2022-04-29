package com.lhr.access

import com.lhr.access.model.analysis.AnalysisModule
import com.lhr.access.model.task.TaskModule

/**
 * @CreateDate: 2022/4/24
 * @Author: mac
 * @Description: 配置信息
 */

val ModelClassList:Array<Class<*>> = arrayOf(
    AnalysisModule::class.java,
    TaskModule::class.java
)