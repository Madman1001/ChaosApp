package com.lhr.access.model.analysis

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.ImageView
import android.widget.TextView
import com.lhr.access.R
import com.lhr.access.model.IHandleModule
import com.lhr.access.utils.AccessibilityUtils
import com.lhr.common.ext.DipToPx
import com.lhr.common.utils.DragWindowTouchListener
import com.lhr.common.ext.visible
import kotlinx.coroutines.*

/**
 * @CreateDate: 2022/4/22
 * @Author: mac
 * @Description: 顶层页面分析模块
 */
class AnalysisModule: IHandleModule {
    private var parent: IHandleModule? = null

    private var logWindowView: View? = null

    private var logTextView: TextView? = null

    private var floatButton: View? = null

    @Volatile
    private var scanJob: Job? = null

    private var topWindowClass = ""

    private var topWindowPackage = ""

    override fun attachHandleModel(handle: IHandleModule) {
        parent = handle
        if (parent is AccessibilityService){
            addLogWindow(parent as AccessibilityService)
            addFloatButton(parent as AccessibilityService)
        }
    }

    override fun onHandle(event: AccessibilityEvent) {
        if (parent is AccessibilityService){
            analysisWindow(parent as AccessibilityService, event)
        }
    }

    override fun detachHandleModel(handle: IHandleModule) {
       parent = null
    }

    private fun analysisWindow(service: AccessibilityService, event: AccessibilityEvent){
        val root = service.rootInActiveWindow ?: return
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            topWindowClass = event.className?.toString() ?: ""
            topWindowPackage = root.packageName.toString()
        }

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            || event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            logTextView?.let {
                scanJob?.cancel()
                scanJob = GlobalScope.launch(Dispatchers.Default) {
                    val id = service.rootInActiveWindow?.windowId ?: 0
                    val str = AccessibilityUtils.traverseTopWindow(service)
                    withContext(Dispatchers.Main){
                        if (id == service.rootInActiveWindow?.windowId ?: 0){
                            it.text = "Top Window: ${topWindowPackage}/${topWindowClass}\n${str}"
                        }
                        scanJob = null
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addLogWindow(service: AccessibilityService){
        val wm =
            service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
        val inflater = LayoutInflater.from(service)
        logWindowView = inflater.inflate(R.layout.hint_window_view, null, false)


        logTextView = logWindowView?.findViewById(R.id.accessibility_window_tv)
        wm.addView(logWindowView, lp)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun addFloatButton(service: AccessibilityService){
        val wm =
            service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
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
        floatButton = ImageView(service).apply {
            setImageDrawable(service.getDrawable(R.mipmap.ic_launch))
            setOnClickListener {
                logTextView?.let {
                    it.visible(!it.visible())
                }
            }
            lp.width = 50.DipToPx.toInt()
            lp.height = 50.DipToPx.toInt()
            setOnTouchListener(DragWindowTouchListener(wm, lp))
        }
        wm.addView(floatButton, lp)
    }
}