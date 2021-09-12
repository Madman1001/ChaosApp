package com.example.sys

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView

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
    }
}