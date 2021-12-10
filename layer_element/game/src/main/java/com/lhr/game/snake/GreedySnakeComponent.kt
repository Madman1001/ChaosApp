package com.lhr.game.snake

/**
 * @author lhr
 * @date 2021/5/10
 * @des 贪吃蛇基本组件
 */
class GreedySnakeComponent(index: Int) {
    private val array: ArrayList<Int> = ArrayList()

    init {
        array.add(index)
    }

    fun addBody(index: Int) {
        array.add(index)
    }

    fun getHead(): Int {
        return array[0]
    }

    fun getNode(index: Int): Int {
        return array[index]
    }

    fun setNode(index: Int, data: Int) {
        array[index] = data
    }

    fun getTail(): Int {
        return array.last()
    }

    fun getLength(): Int{
        return array.size
    }
}