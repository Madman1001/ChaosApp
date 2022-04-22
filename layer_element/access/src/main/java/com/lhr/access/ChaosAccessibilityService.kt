package com.lhr.access

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo.*
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.lhr.access.executor.TaskExecutor
import com.lhr.access.utils.AccessibilityUtils
import com.lhr.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * @author lhr
 * @date 2021/7/2
 * @des
 */
class ChaosAccessibilityService: AccessibilityService() {
    private val tag = "AS_${this::class.java.simpleName}"

    private var hintView: TextView? = null
    override fun onServiceConnected() {
        super.onServiceConnected()
        //获取所有view需要添加FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        this.serviceInfo.flags = this.serviceInfo.flags or FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        addTestWm()
    }

    private fun addTestWm(){
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
        lp.gravity = Gravity.START or Gravity.TOP
        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.hint_window_view, mLayout)
        wm.addView(mLayout, lp)
        mLayout.findViewById<ImageButton>(R.id.accessibility_window_btn).apply {
            //setOnTouchListener(DragTouchListener())
            setOnClickListener {
                hintView?.let {
                    it.visible(!it.visible())
                }
            }
        }
        hintView = mLayout.findViewById(R.id.accessibility_window_tv)
    }


    private var times = 0
    private var topActivity = ""
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
//        Log.e(tag,"event type ${AccessibilityEvent.eventTypeToString(event.eventType)}")
        Log.e(tag,"event type ${event.toString()}")

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            topActivity = event.className.toString()
        }
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            || event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            hintView?.let {
                Log.e("AS_Access", "start traverseTopWindow: ${times++}")
                val run_times = times
                val time = System.currentTimeMillis()
                GlobalScope.launch(Dispatchers.Default) {
                    val id = this@ChaosAccessibilityService?.rootInActiveWindow?.windowId ?: 0
                    val str = AccessibilityUtils.traverseTopWindow(this@ChaosAccessibilityService)
                    withContext(Main){
                        if (id == this@ChaosAccessibilityService?.rootInActiveWindow?.windowId ?: 0){
                            it.text = "top window: <<${topActivity}>>\n${str}"
                        }
                        times--
                        Log.e("AS_Access", "end traverseTopWindow: ${run_times} ${System.currentTimeMillis() - time}")
                    }
                }
            }
        }


        TaskExecutor.acceptActionEvent(this,event)
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