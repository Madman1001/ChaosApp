package com.example.access

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.annotation.SuppressLint
import android.graphics.Path
import android.graphics.Rect
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
    fun backAction(service: AccessibilityService){
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
    }

    @SuppressLint("NewApi")
    fun homeAction(service: AccessibilityService){
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
    }

    @SuppressLint("NewApi")
    fun scrollForwardView(service: AccessibilityService) {
        val info = service.rootInActiveWindow
        val scroll = findNode(info) {
            it.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)
        }
        Log.e(tag, "scrollForwardView $scroll")
        scroll?.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.id)
    }

    @SuppressLint("NewApi")
    fun scrollBackwardView(service: AccessibilityService) {
        val info = service.rootInActiveWindow
        val scroll = findNode(info) {
            it.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD)
        }
        Log.e(tag, "scrollBackwardView $scroll")
        scroll?.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD.id)
    }

    @SuppressLint("NewApi")
    fun clickViewByName(service: AccessibilityService, name: String) {
        val info = service.rootInActiveWindow
        val click = findNode(info) {
            name.equals(it.text?.toString(),true)
        }
        Log.e(tag, "clickView find $name $click")
        if (click != null) {
            //可点击，直接调用点击方法
            if (!performView(click)){
                //不可点击，调用屏幕点击方法 ，24版本以上可用
                val rect = Rect()
                click.getBoundsInScreen(rect)
                val centerX = rect.centerX()
                val centerY = rect.centerY()
                if (!clickScreen(service, centerX, centerY)) {
                    //所以点击均失败
                    Log.e(tag,"点击失败")
                }
            }
        }
    }

    @SuppressLint("NewApi")
    fun checkAllView(service: AccessibilityService) {
        val info = service.rootInActiveWindow
        findNode(info) { false }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun findActionNode(
        root: AccessibilityNodeInfo,
        action: AccessibilityNodeInfo.AccessibilityAction
    ): AccessibilityNodeInfo? {
        return findNode(root) {
            it.actionList.contains(action)
        }
    }

    private fun findNode(
        root: AccessibilityNodeInfo,
        contain: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        val deque: Deque<AccessibilityNodeInfo> = ArrayDeque()
        deque.add(root)
        while (!deque.isEmpty()) {
            val node: AccessibilityNodeInfo = deque.removeFirst()
            Log.e(tag, "findNode:>>>>>>>>>>>>> $node")
            if (contain.invoke(node)) {
                return node
            }
            for (i in 0 until node.childCount) {
                deque.addLast(node.getChild(i))
            }
        }
        return null
    }

    /**
     * 点击屏幕
     */
    private fun clickScreen(service: AccessibilityService, x: Int, y: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val clickPath = Path()
            clickPath.moveTo(x.toFloat(), y.toFloat())
            clickPath.lineTo(x.toFloat(), y.toFloat())
            val gestureBuilder = GestureDescription.Builder()
            gestureBuilder.addStroke(StrokeDescription(clickPath, 0, 300))
            service.dispatchGesture(gestureBuilder.build(), null, null)
            Log.e(tag, "使用AccessibilityService 点击")
            return true
        } else {
            return false
        }
    }

    /**
     * 点击父类布局
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun performView(info: AccessibilityNodeInfo): Boolean {
        if (info.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {
            info.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK.id)
            Log.e(tag,"view 点击成功")
            return true
        }

        var temp:AccessibilityNodeInfo? = info.parent
        while (temp != null) {
            if (temp.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)) {
                temp.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK.id)
                Log.e(tag,"父类view；${temp.className} 点击成功")
                return true
            }
            temp = temp.parent
        }
        return false
    }
}