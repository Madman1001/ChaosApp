package com.lhr.learn.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.addListener
import kotlin.math.max

/**
 * @CreateDate: 2022/9/5
 * @Author: mac
 * @Description: 字符垂直滚动view
 */
open class FlipCharView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyleAttr) {
    companion object {
        const val DEFAULT_FLIP_NUMBER_DURATION = 500L
        const val ONE_HALF = 0.5f
    }

    private val mMaxMoveHeight: Float
        get() = this.height.toFloat()

    /**
     * 字符动画列表
     */
    private var stringList = arrayOf<String>()

    /**
     * 当前字符位置
     */
    private var stringOffsetY = 0f

    private var mTextRect = Rect()

    private var mCharRect = Rect()

    private var mFlipAnimator: Animator? = null

    private var mListener: (Boolean) -> Unit = {}

    init {
        //设置画笔相关属性
        //设置绘制数字样式为实心
        this.paint.style = Paint.Style.FILL
        //设置绘制数字字体加粗
//        this.paint.isFakeBoldText = true
        this.paint.isAntiAlias = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {
            }

            override fun onViewDetachedFromWindow(v: View?) {
                mListener = {}
                if (mFlipAnimator?.isRunning == true) {
                    mFlipAnimator?.cancel()
                }
                mFlipAnimator = null
            }

        })
    }

    fun setText(charList: Array<String>, index: Int = 0) {
        if (mFlipAnimator?.isRunning == true) {
            mFlipAnimator?.cancel()
            mFlipAnimator = null
        }
        stringList = charList
        stringOffsetY = height * index.toFloat()
        invalidate()
    }

    /**
     * 开始
     */
    fun startAnimationAndSetText(charList: Array<String>, animationTime: Long = DEFAULT_FLIP_NUMBER_DURATION){
        if (mFlipAnimator?.isRunning == true) {
            mFlipAnimator?.cancel()
        }
        stringList = charList
        stringOffsetY = 0f
        startFlipAnimation(animationTime)
    }

    fun setAnimationEndListener(listener: (Boolean) -> Unit) {
        mListener = listener
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var mWidth = measuredWidth
        val mHeight = measuredHeight

        val usePaint = paint
        for (s in stringList) {
            mWidth = max(usePaint.measureText(s).toInt(), mWidth)
        }
        setMeasuredDimension(mWidth, mHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas ?: return

        val usePaint = paint
        val offsetY = stringOffsetY

        usePaint.textAlign = Paint.Align.CENTER

        //绘制背景
        mCharRect.set(0, 0, measuredWidth, height)
        onDrawCharBackground(canvas, mCharRect)

        var curY = 0f
        for (s in stringList) {
            usePaint.getTextBounds(s, 0, s.length, mTextRect)
            //绘制文案位置
            canvas.drawText(
                s,
                measuredWidth * ONE_HALF,
                curY - offsetY + (height - paint.ascent()) / 2,
                usePaint
            )
            curY += height
        }

        //绘制前景
        onDrawCharForeground(canvas, mCharRect)
    }

    private fun startFlipAnimation(durationTime: Long) {
        /*
        利用ValueAnimator，在规定时间FLIP_NUMBER_DURATION之内，将值从MAX_MOVE_HEIGHT变为0，
        每次值变化都赋给mNewNumberMoveHeight，同时将mNewNumberMoveHeight - MAX_MOVE_HEIGHT的值赋给mOldNumberMoveHeight，
        并重新绘制，实现新数字和旧数字的上滑；
        */
        val maxMoveDistance = mMaxMoveHeight * (stringList.size - 1)

        val listener = mListener
        mFlipAnimator = ValueAnimator.ofFloat(0f, maxMoveDistance).apply {
            addUpdateListener { animation ->
                val newOffsetY = animation.animatedValue as Float
                stringOffsetY = newOffsetY
                invalidate()
            }
            duration = durationTime
            var isOver = false
            val overAction: (animator: Animator)->Unit = {
                if (!isOver){
                    isOver = true
                    stringOffsetY = maxMoveDistance
                    invalidate()
                    listener.invoke(true)
                }
            }
            this.addListener(
                onEnd = overAction,
                onCancel = overAction
            )
            start()
        }
    }

    open fun onDrawCharBackground(
        canvas: Canvas,
        rect: Rect
    ) {
        //canvas.drawRect(rect, backgroundPaint)
    }

    open fun onDrawCharForeground(
        canvas: Canvas,
        rect: Rect
    ) {
        //canvas.drawRect(rect, foregroundPaint)
    }
}