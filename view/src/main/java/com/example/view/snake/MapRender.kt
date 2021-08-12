package com.example.view.snake

import android.graphics.Canvas
import android.graphics.Paint
import com.example.view.base.BaseMap
import com.example.view.base.BasicRender
import com.example.view.data.MapData

class MapRender: BasicRender() {
    /**
     * 地图数据
     */
    private val map = MapData(row = 20,column = 10)

    /**
     * 边框画笔
     */
    private val framePaint: Paint = Paint()

    /**
     * neir
     */
    private val bodyPaint: Paint = Paint()

    init {
        framePaint.isAntiAlias = true
        framePaint.style = Paint.Style.STROKE
        framePaint.strokeWidth = BaseMap.GameViewConstant.FRAME_STROKE_WIDTH

        bodyPaint.isAntiAlias = true
        bodyPaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas){
        canvas.drawColor(map.mapBackgroundColor)

    }
}