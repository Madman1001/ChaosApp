package com.example.access

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * @author lhr
 * @date 2021/7/2
 * @des
 */
class AccessibilityTestService: AccessibilityService() {

    private val tag = "AccessService"
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName.toString()
        val type = event.eventType
        Log.e(tag,"packagename = $packageName , eventType = $type")

        val rootInActiveWindow = rootInActiveWindow
        if (rootInActiveWindow != null) {
            val findAccessibilityNodeInfosByText: List<*>? =
                rootInActiveWindow.findAccessibilityNodeInfosByText("允许")
            if (findAccessibilityNodeInfosByText != null && !findAccessibilityNodeInfosByText.isEmpty()) {
                for (i in findAccessibilityNodeInfosByText.indices) {
                    (findAccessibilityNodeInfosByText[i] as AccessibilityNodeInfo).performAction(
                        16
                    )
                }
            }
        }
    }

    override fun onInterrupt() {

    }
}