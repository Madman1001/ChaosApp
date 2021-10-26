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

    private val indexPaint: Paint = Paint()

    private var maxSchedule = 5

    private var curSchedule = 4

    constructor(context: Context):super(context)

    constructor(context: Context,attributeSet: AttributeSet):super(context,attributeSet)

    init {
        bgPaint.isAntiAlias = true
        bgPaint.strokeCap = Paint.Cap.ROUND
        bgPaint.strokeWidth = paintWidth
        bgPaint.color = Color.parseColor("#EBEBEB")

        fgPaint.isAntiAlias = true
        fgPaint.strokeCap = Paint.Cap.ROUND
        fgPaint.strokeWidth = paintWidth

        indexPaint.isAntiAlias = true
        indexPaint.style = Paint.Style.FILL
        indexPaint.strokeWidth = paintWidth
        indexPaint.color = Color.parseColor("#FF6414")
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
        val circleDistance = (width - paintWidth * 2f) / (maxSchedule - 1f)
        val startX = paintWidth / 1f
        val startY = height / 3f * 2f

        canvas.drawLine(startX, startY,circleDistance * (maxSchedule - 1f), startY, bgPaint)
        for (i in 0 until maxSchedule){
            canvas.drawCircle(startX + circleDistance * i, startY, paintWidth, bgPaint)
        }
    }

    private fun drawForeground(canvas: Canvas){
        val circleDistance = (width - paintWidth * 2f) / (maxSchedule - 1f)
        val startX = paintWidth / 1f
        val startY = height / 3f * 2f
        val linearGradient = LinearGradient(
            startX,startY,
            circleDistance * (curSchedule - 1),startY,
            Color.parseColor("#FEA235"),Color.parseColor("#FF6414"),
            Shader.TileMode.CLAMP
        )
        fgPaint.shader = linearGradient
        canvas.drawLine(startX,startY,circleDistance * (curSchedule - 1),startY,fgPaint)

        for (i in 0 until curSchedule){
            canvas.drawCircle(startX + circleDistance * i, startY, paintWidth, fgPaint)
        }

        //绘制浮标
        val lastIndexX = startX + circleDistance * (curSchedule - 1)
        val path = Path()
        path.fillType = Path.FillType.EVEN_ODD
        path.moveTo(lastIndexX - paintWidth/2, 0f)
        path.lineTo(lastIndexX - paintWidth/2, 0f)
        path.lineTo(lastIndexX + paintWidth/2, 0f)
        path.lineTo(lastIndexX, 15f)
        path.close()
        canvas.drawPath(path,indexPaint)
    }

}