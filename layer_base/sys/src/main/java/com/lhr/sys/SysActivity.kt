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
import com.lhr.sys.reflection.SysProxyField
import com.lhr.sys.reflection.SysProxyMethod
import java.lang.reflect.Method

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "系统反射")
class SysActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hook()
        val text = TextView(this)
        text.text = "this is SysActivity"
        text.textSize = 30f
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        setContentView(text,params)
        startActivity(Intent(this,PlaceholderActivity::class.java))
    }

    @SuppressLint("PrivateApi")
    private fun hook() {
        val hookClass = HookUtil::class.java
        val proxyMethod = SysProxyMethod(hookClass,"hookInstrumentation",Activity::class.java)
        proxyMethod.invoke(HookUtil,this)

        val proxyField = SysProxyField(hookClass,"hooking")
        proxyField.set(HookUtil,true)
        Log.e("Test",proxyField.get(HookUtil)?.toString() ?: "")
    }
}