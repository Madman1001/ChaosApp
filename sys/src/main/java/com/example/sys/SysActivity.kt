package com.example.sys

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.sys.utils.HookUtil

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
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
    }
}