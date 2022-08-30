package com.lhr.common.ui.widget

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet

/**
 * @CreateDate: 2022/8/30
 * @Author: mac
 * @Description:
 */
class ScrollingTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {
    override fun onFocusChanged(
        focused: Boolean, direction: Int,
        previouslyFocusedRect: Rect?
    ) {
        if (focused) super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }

    override fun onWindowFocusChanged(focused: Boolean) {
        if (focused) super.onWindowFocusChanged(focused)
    }

    override fun isFocused(): Boolean {
        return true
    }
}