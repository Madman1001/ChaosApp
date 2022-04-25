package com.lhr.utils

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.abs

class DragWindowTouchListener(
    private val wm: WindowManager,
    private val param: WindowManager.LayoutParams): View.OnTouchListener{
    private val MAX_DISTANCE = 10
    private var distanceX = 0f
    private var distanceY = 0f

    /*** 是否拦截点击事件 */
    private var isIntercept = false

    /*** 按下时视图的坐标 */
    private var preX: Float = 0f
    private var preY: Float = 0f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                /*按下*/
                isIntercept = false
            }
            MotionEvent.ACTION_MOVE -> {
                /*拖拽*/
                val movX = event.rawX - preX
                val movY = event.rawY - preY
                preX = event.rawX
                preY = event.rawY

                param.x += movX.toInt()
                param.y += movY.toInt()
                wm.updateViewLayout(v, param)

                distanceX += abs(movX)
                distanceY += abs(movY)
                isIntercept = distanceX > MAX_DISTANCE || distanceY > MAX_DISTANCE
            }
            MotionEvent.ACTION_UP -> {
                distanceX = 0f
                distanceY = 0f
                /*松开*/
                return if (isIntercept){
                    isIntercept = false
                    true
                }else{
                    false
                }
            }
        }
        preX = event.rawX
        preY = event.rawY
        return false
    }
}