package com.example.sys

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import com.example.sys.reflection.SysReflection

/**
 * @author lhr
 * @date 2021/6/3
 * @des
 */
class PlaceholderActivity : Activity() {
    @SuppressLint("BlockedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = TextView(this)
        text.text = "this is PlaceholderActivity"
        text.textSize = 30f
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        setContentView(text, params)

        this.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityStopped(activity: Activity) {
                Log.e("Test", "run onResume")
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

        })
        val activityClass = Activity::class.java
        val result = SysReflection.runSysMethod(activityClass,this,"dispatchActivityStopped")
        if (result){
            Log.e("Test", "run dispatchActivityStopped is success")
        }else{
            Log.e("Test", "run dispatchActivityStopped is fail")
        }
    }
}