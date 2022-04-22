package com.lhr.access.model

/**
 * @CreateDate: 2022/4/22
 * @Author: mac
 * @Description: 模块接口
 */
interface IAccessibilityHandle {
    fun attachHandle()

    fun onHandle()

    fun detachHandle()
}