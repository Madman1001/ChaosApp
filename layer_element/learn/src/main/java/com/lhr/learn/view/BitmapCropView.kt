package com.lhr.learn.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.lang.StringBuilder
import java.util.jar.Attributes
import kotlin.math.floor

/**
 * @author lhr
 * @date 2021/8/23
 * @des 图片拖动View
 */
class BitmapCropView: View,View.OnTouchListener {
    private val tag = this::class.java.simpleName

    constructor(context: Context): super(context)
    constructor(context: Context, attributes: AttributeSet?): super(context, attributes)
    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context,attributes, defStyleAttr)

    /**
     * 响应速度
     */
    var mResponseSpeed = 1

    /**
     * 目标图片
     */
    var bitmap: Bitmap? = null
        set(value) {
            field = value
        }

    /**
     * 图片显示区域
     */
    private var bitmapRect = Rect()

    /**
     * 画图区域
     */
    private var canvasRect = Rect()

    /**
     * 可视区域
     */
    private var showRect = Rect()

    init {
        this.setOnTouchListener(this)
    }

    /**
     * 测量
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * 摆放
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        bitmap?.let {
            bitmapRect.set(0,0,it.width,it.height)
            canvasRect.set(0, 0, this.width, this.height)
            showRect = calculateDisplayRect(bitmapRect,canvasRect)
        }
    }

    /**
     * 绘制
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas.drawBitmap(it, showRect, canvasRect, null)
        }
    }

    private var preX = 0f
    private var preY = 0f
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN ->{

            }
            MotionEvent.ACTION_MOVE ->{
                val offsetX = (preX - event.x) * mResponseSpeed
                val offsetY = (preY - event.y) * mResponseSpeed
                val width = showRect.width()
                val height = showRect.height()
                var left = showRect.left
                var right = showRect.right
                var top = showRect.top
                var bottom = showRect.bottom
                if (left + offsetX < 0f){
                    left = 0
                    right = left + width
                }else if (right + offsetX > bitmapRect.width()){
                    right = bitmapRect.width()
                    left = right - width
                }else{
                    left += offsetX.toInt()
                    right += offsetX.toInt()
                }

                if (top + offsetY < 0f){
                    top = 0
                    bottom = top + height
                }else if (bottom + offsetY > bitmapRect.height()){
                    bottom = bitmapRect.height()
                    top = bottom - height
                }else{
                    top += offsetY.toInt()
                    bottom += offsetY.toInt()
                }
                showRect.set(left,top,right,bottom)
                invalidate()
            }
            MotionEvent.ACTION_UP ->{
            }
        }
        preX = event.x
        preY = event.y
        return true
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("bitmapRect = ")
        stringBuilder.append(bitmapRect.toString())
        stringBuilder.append('\n')

        stringBuilder.append("canvasRect = ")
        stringBuilder.append(canvasRect.toString())
        stringBuilder.append('\n')

        stringBuilder.append("showRect = ")
        stringBuilder.append(showRect.toString())
        stringBuilder.append('\n')
        return stringBuilder.toString()
    }

    /**
     * 计算展示的区域
     * @param inRect 输入源区域
     * @param outRect 输出源区域
     */
    fun calculateDisplayRect(inRect: Rect, outRect: Rect): Rect{
        //输出区域的宽高比
        val outScale = (outRect.height() * 1.0f) / (outRect.width() * 1.0f)

        val inWidth = inRect.width()
        val inHeight = inRect.height()

        var newWidth = inWidth
        var newHeight = inHeight
        if (newWidth > newHeight){
            newWidth = (newHeight / outScale).toInt()
            if (newWidth > inWidth){
                newHeight = floor(((inWidth * 1.0f) / (newWidth * 1.0f)) * newHeight).toInt()
                newWidth = inWidth
            }
        }else{
            newHeight = (newWidth * outScale).toInt()
            if (newHeight > inHeight){
                newWidth = floor(((inHeight * 1.0f) / (newHeight * 1.0f)) * newWidth).toInt()
                newHeight = inHeight
            }
        }

        return Rect(0,0,newWidth,newHeight)
    }
}