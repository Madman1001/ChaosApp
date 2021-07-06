package com.example.access

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * @author lhr
 * @date 2021/7/6
 * @des
 */
interface IActionExecutor {
    fun action(service: AccessibilityService, event: AccessibilityEvent)
}