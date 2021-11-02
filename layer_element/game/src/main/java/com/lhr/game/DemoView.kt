package com.lhr.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import com.lhr.game.render.MapRender

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
class DemoView(context: Context): View(context) {
    private val render = MapRender()
    init {
        this.post {
            render.drawRect = Rect(50,50,width - 50,height - 50)
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