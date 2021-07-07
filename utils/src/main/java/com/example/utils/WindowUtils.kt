package com.example.utils

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.TextView


/**
 * Author: lhr
 * Date: 2021/2/24
 * Description:{}
 */
object WindowUtils {
    private val TAG: String = "Test:${WindowUtils::class.java.simpleName}"
    @Volatile private var isAdded = true
    /**
     * 添加视图
     */
    fun addView(windowManager: WindowManager, view: View, windowParams: WindowManager.LayoutParams?) {
        val types = arrayOf(
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.TYPE_TOAST,
            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
            WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
            WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
            2029,
            2033, /*WindowManager.LayoutParams.TYPE_VOICE_INTERACTION_STARTING*/
            2034, /*WindowManager.LayoutParams.TYPE_DOCK_DIVIDER*/
            2037, /*WindowManager.LayoutParams.TYPE_PRESENTATION*/
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        isAdded = false
        for (type in types) {
            try {
                windowParams?.type = type
                val temp = TextView(view.context)
                temp.post {
                    try {
                        if (!isAdded) {
                            windowParams?.type = type
                            windowManager.addView(view, windowParams)
                            isAdded = true
                        }
                        removeView(windowManager, temp)
                    }catch (e:Exception){}
                }
                windowManager.addView(temp, windowParams)
            } catch (e: Exception) {
            }
        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Log.e(TAG, if (Settings.canDrawOverlays(context)) "授权" else "不授权")
//        } else {
//            Log.e(TAG, "8.0以下不需要授权")
//        }
    }

    /**
     * 移除视图
     */
    fun removeView(context: Context, view: View) {
        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.removeView(view)
        } catch (e:Exception){

        }
    }

    fun removeView(windowManager: WindowManager,view: View){
        try {
            windowManager.removeView(view)
        } catch (e:Exception){

        }
    }

}
