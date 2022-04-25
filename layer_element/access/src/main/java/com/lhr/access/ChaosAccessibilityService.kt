package com.lhr.access

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo.*
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi
import com.lhr.access.model.IHandleModule


/**
 * @author lhr
 * @date 2021/7/2
 * @des 无障碍服务
 */
class ChaosAccessibilityService: AccessibilityService(), IHandleModule{
    private val tag = "AS_${this::class.java.simpleName}"

    private val mHandlerModelList = ArrayList<IHandleModule>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        //获取所有view需要添加FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        this.serviceInfo.flags =
            this.serviceInfo.flags or FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or FLAG_RETRIEVE_INTERACTIVE_WINDOWS

        for (model in ModelClassList){
            model.newInstance().let {
                if (it is IHandleModule){
                    attachHandleModel(it)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        Log.d(tag,"event type ${event.toString()}")
        onHandle(event)
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

    override fun onUnbind(intent: Intent?): Boolean {
        for (module in mHandlerModelList) {
            kotlin.runCatching {
                detachHandleModel(module)
            }
        }
        mHandlerModelList.clear()
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        Log.e(tag,"onInterrupt")
    }

    override fun attachHandleModel(handle: IHandleModule) {
        if (!mHandlerModelList.contains(handle)){
            mHandlerModelList.add(handle)
            handle.attachHandleModel(this)
        }
    }

    override fun onHandle(event: AccessibilityEvent) {
        for (iHandleModel in mHandlerModelList) {
            iHandleModel.onHandle(event)
        }
    }

    override fun detachHandleModel(handle: IHandleModule) {
        if (mHandlerModelList.remove(handle)) {
            handle.detachHandleModel(this)
        }
    }
}