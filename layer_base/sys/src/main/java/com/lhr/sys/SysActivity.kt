package com.lhr.sys

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.lhr.sys.utils.HookUtil
import com.lhr.centre.annotation.CElement
import com.lhr.sys.proxy.ProxyInstrumentation
import com.lhr.sys.reflection.SysProxyField
import com.lhr.sys.reflection.SysProxyMethod


/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "系统反射")
class SysActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sys_activity)
    }

    fun onHook(v: View){
        hookInstrumentation()
    }

    fun onStartActivity(v: View){
        startActivity(Intent(this,PlaceholderActivity::class.java))
    }

    @SuppressLint("PrivateApi")
    private fun hookInstrumentation() {
        val hookClass = HookUtil::class.java
        val proxyMethod = SysProxyMethod(hookClass,"hookInstrumentation",Activity::class.java, Class::class.java)
        proxyMethod.invoke(HookUtil,this, ProxyInstrumentation::class.java)
    }
}