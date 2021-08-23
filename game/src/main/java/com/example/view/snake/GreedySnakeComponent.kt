package com.example.view.snake

/**
 * @author lhr
 * @date 2021/5/10
 * @des 贪吃蛇基本组件
 */
class GreedySnakeComponent(index:Int){
    val array: ArrayList<Int> = ArrayList()
    init {
        array.add(index)
    }
}