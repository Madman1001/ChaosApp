package com.example.anim

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * @author lhr
 * @date 2021/10/24
 * @des 自定义进度条
 */
class GameSeekBar : View{
    /**
     * 画笔宽度
     */
    private val paintWidth = 35f

    private val bgPaint: Paint = Paint()

    private val fgPaint: Paint = Paint()

    private var maxSchedule = 5

    private var curSchedule = 4

    constructor(context: Context):super(context)

    constructor(context: Context,attributeSet: AttributeSet):super(context,attributeSet)

    init {
        bgPaint.isAntiAlias = true
        bgPaint.strokeCap = Paint.Cap.ROUND
        bgPaint.strokeWidth = paintWidth
        bgPaint.color = Color.GRAY

        fgPaint.isAntiAlias = true
        fgPaint.strokeCap = Paint.Cap.ROUND
        fgPaint.strokeWidth = paintWidth
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBackground(canvas)

        drawForeground(canvas)
    }

    private fun drawBackground(canvas: Canvas){
        val circleDistance = width / (maxSchedule - 1f)

        canvas.drawLine(0f,height/2f,circleDistance * (maxSchedule - 1f),height/2f,bgPaint)
        for (i in 0 until maxSchedule){
            canvas.drawCircle(circleDistance * i, height/2f, paintWidth, bgPaint)
        }
    }

    private fun drawForeground(canvas: Canvas){
        val circleDistance = width / (maxSchedule - 1f)
        val linearGradient = LinearGradient(
            0f,height/2f,
            circleDistance * (curSchedule - 1),height/2f,
            Color.parseColor("#ff0000"),Color.parseColor("#00ff00"),
            Shader.TileMode.CLAMP
        )
        fgPaint.shader = linearGradient
        canvas.drawLine(0f,height/2f,circleDistance * (curSchedule - 1),height/2f,fgPaint)

        for (i in 0 until curSchedule){
            canvas.drawCircle(circleDistance * i, height/2f, paintWidth, fgPaint)
        }
    }

}