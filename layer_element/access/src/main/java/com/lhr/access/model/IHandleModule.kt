package com.lhr.access.model

import android.view.accessibility.AccessibilityEvent

/**
 * @CreateDate: 2022/4/24
 * @Author: mac
 * @Description: 处理模块
 */
interface IHandleModule {
    fun attachHandleModel(handle: IHandleModule)

    fun onHandle(event: AccessibilityEvent)

    fun detachHandleModel(handle: IHandleModule)
}