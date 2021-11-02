package com.lhr.sys

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import com.lhr.sys.utils.HookUtil
import com.lhr.centre.annotation.CElement
import java.lang.reflect.Method

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "系统反射")
class SysActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        HookUtil.hookInstrumentation(this)
        super.onCreate(savedInstanceState)
        val text = TextView(this)
        text.text = "this is SysActivity"
        text.textSize = 30f
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        setContentView(text,params)
        startActivity(Intent(this,PlaceholderActivity::class.java))
        test()
    }

    @SuppressLint("PrivateApi")
    private fun test() {
        val activityThread =
            Class.forName("android.app.ActivityThread")
        val hclass = Class.forName("android.app.ActivityThread\$H")
        val declaredMethods: Array<Method> = hclass.declaredMethods
        for (declaredMethod in declaredMethods) {
            Log.e("Test", "declareField: $declaredMethod")
        }
    }
}