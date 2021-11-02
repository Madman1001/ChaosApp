package com.lhr.sys

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView

/**
 * @author lhr
 * @date 2021/6/3
 * @des
 */
class HookActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = TextView(this)
        text.text = "this is HookActivity"
        text.textSize = 30f
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setContentView(text,params)
    }
}