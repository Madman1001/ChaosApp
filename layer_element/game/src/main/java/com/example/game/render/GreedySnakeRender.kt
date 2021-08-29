package com.example.game.render

import android.graphics.Canvas
import com.example.game.data.MapData

/**
 * @author lhr
 * @date 2021/5/7
 * @des 贪吃蛇渲染器
 */
class GreedySnakeRender(): IRender {
    /**
     * 地图数据
     */
    private val map = MapData(row = 20,column = 10)


    override fun onRender(canvas: Canvas) {
    }

}