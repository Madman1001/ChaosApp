package com.example.view.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.example.view.data.MapData

/**
 * @author lhr
 * @date 2021/5/10
 * @des 像素地图基础绘制类
 */
class MapRender(val map: MapData = MapData(row = 20,column = 10)) :
    IRender {

    val row = map.row

    val column = map.column

    /**
     * 绘画区域
     */
    var rect : Rect = Rect(0,0,0,0)

    /**
     * 边框画笔
     */
    private val framePaint: Paint = Paint()

    /**
     * 内容画笔
     */
    private val bodyPaint: Paint = Paint()

    init {
        framePaint.isAntiAlias = true
        framePaint.style = Paint.Style.STROKE
        framePaint.strokeWidth = map.frameStrokeWidth
        framePaint.color = map.mapFrameStrokeColor

        bodyPaint.isAntiAlias = true
        bodyPaint.style = Paint.Style.FILL
    }

    override fun onRender(canvas: Canvas){
        canvas.drawColor(map.mapBackgroundColor)
        canvas.drawRect(rect, framePaint)
    }
}