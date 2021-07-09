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
import com.example.access.action.AccessibilityActionExecutor
import com.example.access.action.setting.WindowSettingTask
import com.example.access.utils.AccessibilityUtils


/**
 * @author lhr
 * @date 2021/7/2
 * @des
 */
class AccessibilityTestService: AccessibilityService() {
    private val tag = "AccessService"

    override fun onServiceConnected() {
        super.onServiceConnected()
        //获取所有view需要添加FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        this.serviceInfo.flags = this.serviceInfo.flags or FLAG_INCLUDE_NOT_IMPORTANT_VIEWS

        // 创建测试按钮
        val wm =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val mLayout = FrameLayout(this)
        val lp = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        }else{
            lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }
        lp.format = PixelFormat.TRANSLUCENT
        lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.action_bar, mLayout)
        wm.addView(mLayout, lp)
        AccessibilityActionExecutor.postAction(WindowSettingTask())
        mLayout.findViewById<View>(R.id.accessibility_scroll_forward_action).setOnClickListener {
            AccessibilityUtils.scrollForwardView(this)
        }
        mLayout.findViewById<View>(R.id.accessibility_scroll_backward_action).setOnClickListener {
            AccessibilityUtils.scrollBackwardView(this)
        }
        mLayout.findViewById<View>(R.id.accessibility_click_action).setOnClickListener {
            AccessibilityUtils.clickViewByName(this,"始终允许")
//            TestAction.checkAllView(this)
        }
        mLayout.findViewById<View>(R.id.accessibility_back_action).setOnClickListener {
            AccessibilityUtils.backAction(this)
        }
        mLayout.findViewById<View>(R.id.accessibility_setting_action).setOnClickListener {
            AccessibilityActionExecutor.startAction(this)
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.e(tag,"event type ${AccessibilityEvent.eventTypeToString(event.eventType)}")

        AccessibilityActionExecutor.acceptActionEvent(this,event)
        /*
        视图获得焦点 AccessibilityEvent.TYPE_VIEW_FOCUSED打开一个新界面时以此事件为开始
         */

        /*
        窗体发生变化 AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
         */

        /*
        窗体内容发生变化 AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
         */

        /*
        发生点击事件 AccessibilityEvent.TYPE_VIEW_CLICKED
         */
    }

    override fun onInterrupt() {
        Log.e(tag,"onInterrupt")
    }
}