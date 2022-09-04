package com.lhr.common.ext

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.getSystemService

/**
 * @CreateDate: 2022/4/22
 * @Author: mac
 * @Description:View 扩展方法
 */
fun View.visible(bool: Boolean){
    this.visibility = if (bool) View.VISIBLE else View.GONE
}

fun View.visible(): Boolean{
    return this.visibility == View.VISIBLE
}

@SuppressLint("ClickableViewAccessibility")
fun EditText.initInputBar(root: ViewGroup){
    root.run {
        val focusView = View(context)
        val param = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        addView(focusView, param)
        val rect = Rect()
        focusView.setOnTouchListener(View.OnTouchListener { v, event ->
            if (this@initInputBar.isFocused) {
                val position = IntArray(2) { 0 }
                this@initInputBar.getLocationInWindow(position)
                rect.set(position[0], position[1], position[0] + this@initInputBar.width, position[1] + this@initInputBar.height)
                if (!rect.contains(event.rawX.toInt(), event.rawY.toInt())){
                    hideSoftInput(focusView)
                    return@OnTouchListener true
                }
            }
            return@OnTouchListener false
        })
    }
}

private var inputManager: InputMethodManager? = null

private fun hideSoftInput(view: View) {
    view.run {
        val manager = inputManager ?: context.getSystemService()
        if (inputManager == null) {
            inputManager = manager
        }
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
        inputManager?.hideSoftInputFromWindow(windowToken, 0)
    }
}