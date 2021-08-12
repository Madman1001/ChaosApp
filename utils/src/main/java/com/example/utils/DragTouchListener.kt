package com.example.utils

import android.graphics.Point
import android.view.MotionEvent
import android.view.PointerIcon
import android.view.View
import android.view.ViewGroup
import kotlin.math.abs

class DragTouchListener(private val isReturn: Boolean = false): View.OnTouchListener{
    private val MAX_DISTANCE = 10
    private var distanceX = 0f
    private var distanceY = 0f

    /*** 是否拦截点击事件 */
    private var isIntercept = false

    /*** 按下时的坐标 */
    private var downX:Float = 0f
    private var downY:Float = 0f

    /*** 按下时视图的坐标 */
    private var preViewX: Float = 0f
    private var preViewY: Float = 0f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                /*按下*/
                isIntercept = false
                downX = event.x
                downY = event.y
                preViewX = v.x
                preViewY = v.y
            }
            MotionEvent.ACTION_MOVE -> {
                /*拖拽*/
                val movX = event.x - downX
                val movY = event.y - downY
                v.let {
                    val parentView = it.parent as ViewGroup
                    var tX = it.x + movX
                    if (tX > parentView.width - it.width){
                        tX = (parentView.width - it.width).toFloat()
                    }else if (tX < 0){
                        tX = 0f
                    }
                    var tY = it.y + movY
                    if (tY > parentView.height - it.height){
                        tY = (parentView.height - it.height).toFloat()
                    }else if (tY < 0){
                        tY = 0f
                    }
                    it.x = tX
                    it.y = tY
                    it.invalidate()
                }
                distanceX += abs(movX)
                distanceY += abs(movY)
                isIntercept = distanceX > MAX_DISTANCE || distanceY > MAX_DISTANCE
            }
            MotionEvent.ACTION_UP -> {
                if (isReturn){
                    v.x = preViewX
                    v.y = preViewY
                }
                /*松开*/
                return if (isIntercept){
                    isIntercept = false
                    distanceX = 0f
                    distanceY = 0f
                    true
                }else{
                    false
                }
            }
        }
        return false
    }
}