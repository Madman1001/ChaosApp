package com.lhr.learn.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.FloatRange
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * @author lhr
 * @date 6/12/2022
 * @des
 */
class ABImageView: View {
    //分割布局
    @FloatRange(from = 0.0, to = 1.0)
    var divideValue: Float = 0.5F
        set(value) {
            field = value
            invalidate()
        }

    private var aBitmap: Bitmap? = null
    private var bBitmap: Bitmap? = null

    var maxWidth = Int.MAX_VALUE
        set(value) {
            field = value
            requestLayout()
        }

    var maxHeight = Int.MAX_VALUE
        set(value) {
            field = value
            requestLayout()
        }

    private var mDrawableWidth = 0
    private var mDrawableHeight = 0

    private val tempBitmapRect by lazy { Rect() }
    private val tempClipRectF by lazy { RectF() }
    private val tempDrawRect by lazy { Rect() }

    private var isBitmapChange = false
    var mBitmapChangeListener: ()->Unit = {}

    constructor(context: Context):super(context)
    constructor(context: Context, attrs: AttributeSet?):super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr)

    fun setupBitmaps(aBitmap: Bitmap?, bBitmap: Bitmap?){
        if (this.aBitmap != null && this.aBitmap?.isRecycled == false) this.aBitmap?.recycle()
        if (this.bBitmap != null && this.bBitmap?.isRecycled == false) this.bBitmap?.recycle()
        this.aBitmap = aBitmap
        this.bBitmap = bBitmap
        if (aBitmap != null){
            mDrawableWidth = aBitmap.width
            mDrawableHeight = aBitmap.height
        } else if (bBitmap != null){
            mDrawableWidth = bBitmap.width
            mDrawableHeight = bBitmap.height
        } else {
            mDrawableWidth = 0
            mDrawableHeight = 0
        }

        isBitmapChange = true
        invalidate()
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        var desiredAspect = 0f
        var w = 0
        var h = 0
        if (mDrawableWidth > 0 && mDrawableHeight > 0){
            w = mDrawableWidth
            h = mDrawableHeight
            desiredAspect = w.toFloat() / h.toFloat()
        }
        if (w <= 0) w = 1
        if (h <= 0) h = 1

        val resizeWidth = widthSpecMode != MeasureSpec.EXACTLY
        val resizeHeight = heightSpecMode != MeasureSpec.EXACTLY

        var widthSize: Int
        var heightSize: Int
        if (desiredAspect != 0f){
            widthSize = resolveAdjustedSize(w, maxWidth, widthMeasureSpec)
            if (resizeWidth){
                widthSize = max(widthSpecSize, widthSize)
            }
            heightSize = resolveAdjustedSize(h, maxHeight, heightMeasureSpec)
            if (resizeHeight){
                heightSize = max(heightSpecSize, widthSize)
            }

            val actualAspect: Float = widthSize.toFloat() / heightSize
            if (abs(actualAspect - desiredAspect) > 0.0000001){
                var done = false
                // Try adjusting width to be proportional to height
                if (resizeWidth) {
                    val newWidth: Int = (desiredAspect * heightSize).toInt()
                    if (!resizeHeight) {
                        widthSize = resolveAdjustedSize(newWidth, maxWidth, widthMeasureSpec)
                    }
                    if (newWidth <= widthSize) {
                        widthSize = newWidth
                        done = true
                    }
                }
                // Try adjusting height to be proportional to width
                if (!done && resizeHeight) {
                    val newHeight: Int = (widthSize / desiredAspect).toInt()
                    // Allow the height to outgrow its original estimate if width is fixed.
                    if (!resizeWidth) {
                        heightSize = resolveAdjustedSize(newHeight, maxHeight, heightMeasureSpec)
                    }
                    if (newHeight <= heightSize) {
                        heightSize = newHeight
                    }
                }
            }
        } else {
            w = max(w, suggestedMinimumWidth)
            h = max(h, suggestedMinimumHeight)
            widthSize = resolveSizeAndState(w, widthMeasureSpec, 0)
            heightSize = resolveSizeAndState(h, heightMeasureSpec, 0)
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isBitmapChange){
            isBitmapChange = false
            mBitmapChangeListener.invoke()
        }

        tempDrawRect.set(0, 0, width, height)

        if (aBitmap != null) {
            //draw A bitmap
            canvas.save()
            tempBitmapRect.set(0, 0, aBitmap?.width?:0, aBitmap?.height?:0)
            tempClipRectF.set(0f, 0f, width * divideValue, height.toFloat())
            canvas.clipRect(tempClipRectF)
            canvas.drawBitmap(aBitmap!!, tempBitmapRect, tempDrawRect, null)
            canvas.restore()
        }
        if (bBitmap != null) {
            //draw B bitmap
            canvas.save()
            tempBitmapRect.set(0, 0, bBitmap?.width?:0, bBitmap?.height?:0)
            tempClipRectF.set(width * divideValue, 0f, width.toFloat(), height.toFloat())
            canvas.clipRect(tempClipRectF)
            canvas.drawBitmap(bBitmap!!, tempBitmapRect, tempDrawRect, null)
            canvas.restore()
        }
    }

    private fun resolveAdjustedSize(
        desiredSize: Int,
        maxSize: Int,
        measureSpec: Int
    ): Int {
        var result = desiredSize
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        when (specMode) {
            MeasureSpec.UNSPECIFIED -> result = min(desiredSize, maxSize)
            MeasureSpec.AT_MOST -> result = min(min(desiredSize, specSize), maxSize)
            MeasureSpec.EXACTLY -> result = specSize
        }
        return result
    }
}