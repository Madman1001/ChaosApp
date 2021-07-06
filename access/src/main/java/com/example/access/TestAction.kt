package com.example.access

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import java.util.*


/**
 * @author lhr
 * @date 2021/7/6
 * @des
 */
object TestAction {
    private val tag = "TestAction"
    @SuppressLint("NewApi")
    fun scrollForwardView(service: AccessibilityService) {
        val info = service.rootInActiveWindow
        val scroll = findScrollableNode(info)
        Log.e(tag,"scrollForwardView $scroll")
        scroll?.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
    }

    @SuppressLint("NewApi")
    fun scrollBackwardView(service: AccessibilityService) {
        val info = service.rootInActiveWindow
        val scroll = findScrollableNode(info)
        Log.e(tag,"scrollBackwardView $scroll")
        scroll?.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD.id)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val deque: Deque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)
        while (!deque.isEmpty()) {
            val node: AccessibilityNodeInfo = deque.removeFirst()
            Log.e(tag,"findScrollableNode ${node.className}")
            if (node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node
            }
            for (i in 0 until node.childCount) {
                deque.addLast(node.getChild(i))
            }
        }
        return null
    }
}