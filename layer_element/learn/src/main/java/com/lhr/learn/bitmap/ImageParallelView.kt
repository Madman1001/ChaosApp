package com.lhr.learn.bitmap

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.lhr.learn.R
import kotlin.math.abs

/**
 * @author lhr
 * @date 6/12/2022
 * @des
 */
class ImageParallelView: FrameLayout {
    constructor(context: Context):super(context)
    constructor(context: Context, attrs: AttributeSet?):super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr)

    private val abImageView: ABImageView
    val aTextView: TextView
    val bTextView: TextView
    private val divideView: View
    private val mutualView: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.image_parallel_view_layout, this)
        abImageView = this.findViewById(R.id.abImageView)
        aTextView = this.findViewById(R.id.aTextView)
        bTextView = this.findViewById(R.id.bTextView)
        divideView = this.findViewById(R.id.divideView)
        mutualView = this.findViewById(R.id.mutualView)
        setupMutualDragAction()
        abImageView.mBitmapChangeListener = {
            updateDivide(abImageView.divideValue)
        }
    }

    fun setupParallelBitmap(leftBitmap: Bitmap, rightBitmap: Bitmap){
        abImageView.setupBitmaps(leftBitmap, rightBitmap)
        post { requestLayout() }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun canDrag(bool: Boolean){
        if (bool){
            setupMutualDragAction()
        } else {
            mutualView.setOnTouchListener(null)
        }
    }

    fun setSchedule(schedule: Float){
        abImageView.post {
            updateDivide(schedule)
        }
    }

    private fun updateDivide(schedule: Float) {
        abImageView.divideValue = schedule
        val centerIndex = abImageView.divideValue * abImageView.width + abImageView.x

        //set mutual View index
        mutualView.x = centerIndex - mutualView.width.toFloat() / 2
        //set divide view index
        divideView.x = centerIndex - divideView.width.toFloat() / 2

        aTextView.x = divideView.x - (aTextView.width - divideView.width)
        bTextView.x = divideView.x
        post {
            requestLayout()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupMutualDragAction(){
        val touchListener = object : OnTouchListener{
            private var preX = 0f
            private var minMoveDistance = 4
            private var mResponseSpeed = 1
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when(event.action){
                    MotionEvent.ACTION_MOVE ->{
                        v.parent.requestDisallowInterceptTouchEvent(true)
                        //只能水平滑动
                        val offsetX = (event.rawX - preX) * mResponseSpeed
                        if (minMoveDistance < abs(offsetX)){
                            var curDivide = abImageView.divideValue
                            curDivide += (offsetX / abImageView.width.toFloat())
                            if (offsetX < 0 && curDivide < 0){
                                curDivide = 0f
                            } else if (offsetX > 0 && curDivide > 1.0f){
                                curDivide = 1f
                            }
                            updateDivide(curDivide)
                        }
                    }
                    MotionEvent.ACTION_DOWN ->{
                        v.parent.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_UP ->{
                        v.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                preX = event.rawX
                return true
            }
        }
        mutualView.setOnTouchListener(touchListener)
    }
}