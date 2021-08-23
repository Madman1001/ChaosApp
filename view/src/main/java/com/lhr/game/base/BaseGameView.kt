package com.lhr.game.base

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point

open class BaseGameView (val canvas: Canvas, protected val row: Int = 20, protected val column: Int = 10) {
    protected val BACKGROUND_COLOR = Color.parseColor("#9EAD86")
    protected val BODY_GRAY_COLOR = Color.parseColor("#879372")
    protected val FOREGROUND_COLOR = Color.parseColor("#000000")

    object GameViewConstant {
        /**
         * 边框宽度
         */
        const val FRAME_STROKE_WIDTH = 8f
    }

    /**
     * 像素矩阵
     */
    protected val spaceArray = Array<Boolean>(row * column) { false }

    /**
     * 开始坐标
     */
    protected val startPoint: Point = Point(0, 0)

    /**
     * 结束坐标
     */
    protected val endPoint: Point = Point(0, 0)

    protected val framePaint = Paint()

    protected val bodyPaint = Paint()

    /**
     * 方块边长
     */
    protected var sideLen = 0

    init {
        framePaint.isAntiAlias = true
        framePaint.style = Paint.Style.STROKE
        framePaint.strokeWidth = GameViewConstant.FRAME_STROKE_WIDTH

        bodyPaint.isAntiAlias = true
        bodyPaint.style = Paint.Style.FILL
        val widthLen: Int = (canvas.width - 100) / column
        val heightLen: Int = (canvas.height - 100) / row
        sideLen = if (widthLen < heightLen) {
            widthLen
        } else {
            heightLen
        }
        val paddingHorizontal: Int =
            (canvas.width - sideLen * column + framePaint.strokeWidth).toInt()
        val paddingVertical: Int =
            (canvas.height - sideLen * row + framePaint.strokeWidth).toInt()
        startPoint.x = paddingHorizontal / 2
        startPoint.y = paddingVertical / 2
        endPoint.x = canvas.width - paddingHorizontal / 2
        endPoint.y = canvas.height - paddingVertical / 2
    }

    fun draw(canvas: Canvas){
        onDrawBackground(canvas)
        onDraw(canvas)
        onDrawForeground(canvas)
    }

    protected fun onDrawBackground(canvas: Canvas){

    }

    protected fun onDraw(canvas: Canvas){
        canvas.drawColor(BACKGROUND_COLOR)
        framePaint.color = FOREGROUND_COLOR
        canvas.drawRect(
            startPoint.x.toFloat() - framePaint.strokeWidth,
            startPoint.y.toFloat() - framePaint.strokeWidth,
            endPoint.x.toFloat() + framePaint.strokeWidth * 2,
            endPoint.y.toFloat() + framePaint.strokeWidth * 2,
            framePaint
        )

        for (i in spaceArray.indices) {
            framePaint.color = if (spaceArray[i]) FOREGROUND_COLOR else BODY_GRAY_COLOR
            bodyPaint.color = if (spaceArray[i]) FOREGROUND_COLOR else BODY_GRAY_COLOR
            val len = sideLen
            val left: Float = ((i % column) * len + startPoint.x).toFloat()
            val top: Float = ((i / column) * len + startPoint.y).toFloat()
            val right: Float = left + len
            val bottom: Float = top + len
            val frameGap = framePaint.strokeWidth * 1.05f
            canvas.drawRect(
                left + frameGap,
                top + frameGap,
                right - frameGap,
                bottom - frameGap,
                framePaint
            )

            val bodyGap = len / 7f + frameGap
            canvas.drawRect(
                left + bodyGap,
                top + bodyGap,
                right - bodyGap,
                bottom - bodyGap,
                bodyPaint
            )
        }
    }


    protected fun onDrawForeground(canvas: Canvas){

    }

    protected fun clearMap(){
        spaceArray.fill(false)
    }
}