package com.lhr.game.base

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.view.View
import com.lhr.game.base.BaseMap.GameViewConstant.FRAME_STROKE_WIDTH

/**
 * @author lhr
 * @date 2021/5/12
 * @des
 */
open class BaseMap(context: Context, protected val row: Int = 20, protected val column: Int = 10) :
    View(context) {

    protected val BACKGROUND_COLOR = Color.parseColor("#9EAD86")
    protected val BODY_GRAY_COLOR = Color.parseColor("#879372")
    protected val BODY_BLACK_COLOR = Color.parseColor("#000000")

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

    protected var sideLen = 0

    init {
        framePaint.isAntiAlias = true
        framePaint.style = Paint.Style.STROKE
        framePaint.strokeWidth = FRAME_STROKE_WIDTH

        bodyPaint.isAntiAlias = true
        bodyPaint.style = Paint.Style.FILL

        this.post {
            val widthLen: Int = (width - 100) / column
            val heightLen: Int = (height - 100) / row
            sideLen = if (widthLen < heightLen) {
                widthLen
            } else {
                heightLen
            }
            val paddingHorizontal: Int =
                (this.width - sideLen * column + framePaint.strokeWidth).toInt()
            val paddingVertical: Int =
                (this.height - sideLen * row + framePaint.strokeWidth).toInt()
            startPoint.x = paddingHorizontal / 2
            startPoint.y = paddingVertical / 2
            endPoint.x = this.width - paddingHorizontal / 2
            endPoint.y = this.height - paddingVertical / 2
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
        canvas.drawColor(BACKGROUND_COLOR)
        framePaint.color = BODY_BLACK_COLOR
        canvas.drawRect(
            startPoint.x.toFloat() - framePaint.strokeWidth,
            startPoint.y.toFloat() - framePaint.strokeWidth,
            endPoint.x.toFloat() + framePaint.strokeWidth * 2,
            endPoint.y.toFloat() + framePaint.strokeWidth * 2,
            framePaint
        )

        for (i in spaceArray.indices) {
            framePaint.color = if (spaceArray[i]) BODY_BLACK_COLOR else BODY_GRAY_COLOR
            bodyPaint.color = if (spaceArray[i]) BODY_BLACK_COLOR else BODY_GRAY_COLOR
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

    protected fun clearMap(){
        spaceArray.fill(false)
    }

    object GameViewConstant {
        /**
         * 边框宽度
         */
        const val FRAME_STROKE_WIDTH = 8f
    }
}