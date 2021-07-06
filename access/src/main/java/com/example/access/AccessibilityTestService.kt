package com.example.access

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import androidx.annotation.RequiresApi


/**
 * @author lhr
 * @date 2021/7/2
 * @des
 */
class AccessibilityTestService: AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        //获取所有view需要添加FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        this.serviceInfo.flags = this.serviceInfo.flags or FLAG_INCLUDE_NOT_IMPORTANT_VIEWS

        // 创建测试按钮
        val wm =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val mLayout = FrameLayout(this)
        val lp = WindowManager.LayoutParams()
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        lp.format = PixelFormat.TRANSLUCENT
        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.action_bar, mLayout)
        wm.addView(mLayout, lp)
        mLayout.findViewById<View>(R.id.accessibility_scroll_forward_action).setOnClickListener {
            TestAction.scrollForwardView(this)
        }
        mLayout.findViewById<View>(R.id.accessibility_scroll_backward_action).setOnClickListener {
            TestAction.scrollBackwardView(this)
        }
    }

    private val tag = "AccessService"
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.e(tag,"event type ${AccessibilityEvent.eventTypeToString(event.eventType)}")

    }

    override fun onInterrupt() {
        Log.e(tag,"onInterrupt")
    }

    private val action = object : IActionExecutor{
        override fun action(service: AccessibilityService,event: AccessibilityEvent) {
            val info = event.source
            val count = info.childCount
            Log.e(tag,"action info: ${info.className}")
            for (i in 0 until count){
                val child = info.getChild(i)
                Log.e(tag,"action child: ${child.className}")
            }
        }
    }
}