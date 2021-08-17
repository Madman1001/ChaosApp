package com.example.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import com.example.view.render.MapRender

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
class DemoView(context: Context): View(context) {
    private val render = MapRender()
    init {
        this.post {
            render.rect = Rect(100,100,width - 100,height - 100)
            invalidate()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        render.onRender(canvas)
    }
}