package com.example.game.data

import android.graphics.Color

/**
 * @author lhr
 * @date 2021/5/12
 * @des 数据类
 */
data class MapData(val row: Int, val column: Int) {

    /**
     * 像素矩阵
     */
    val spaceArray = Array<Boolean>(row * column) { false }

    /**
     * 地图背景色
     */
    var mapBackgroundColor = Color.parseColor("#9EAD86")

    /**
     * 方块背景色
     */
    var bodyBackgroundColor = Color.parseColor("#879372")

    /**
     * 方块前景色
     */
    var bodyForegroundColor = Color.parseColor("#000000")

    /**
     * 边框颜色
     */
    var mapFrameStrokeColor = Color.parseColor("#000000")

    /**
     * 边框厚度
     */
    var frameStrokeWidth = 8f

}