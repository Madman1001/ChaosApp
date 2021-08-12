package com.example.view.snake

import android.content.Context
import android.view.View
import com.example.view.base.BaseMap
import com.example.view.input.IActionInput
import com.example.view.power.ActionPower

/**
 * @author lhr
 * @date 2021/5/7
 * @des 贪吃蛇
 */
class GreedySnakeView(context: Context, row: Int = 20, column: Int = 10) : BaseMap(context,row, column), IActionInput {
    private var snake = GreedySnakeComponent(9)
    var direction = Direction.GO_UP
    var listener: SnakeListener? = null
    private var fruit: Int = -1
        set(value) {
            field = value
            listener?.generateFruit(field % column, field / column)
        }
    private var previousTail: Int = -1

    init {
        this.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View) {
                ActionPower.stopRun()
            }

            override fun onViewAttachedToWindow(v: View) {
                fruit = generateFruit()
                ActionPower.startRun(100L, this@GreedySnakeView::updateView)
            }
        })
    }

    private fun updateView() {
        if (fruit == -1){
            //win
            ActionPower.stopRun()
        }
        when (direction) {
            Direction.GO_LEFT -> goLeft()
            Direction.GO_RIGHT -> goRight()
            Direction.GO_UP -> goUp()
            Direction.GO_DOWN -> goDown()
        }
        listener?.updateView(snake.array[0] % column,snake.array[0] / column)
        if (checkImpact()) {
            eatFruit()
            fruit = generateFruit()
        }

        spaceArray.fill(false)
        snake.array.forEach {
            spaceArray[it] = true
        }
        if (fruit in spaceArray.indices) {
            spaceArray[fruit] = true
        }

        invalidate()
    }

    override fun goLeft() {
        val step = if (snake.array[0] % column <= 0) {
            column - 1
        } else {
            -1
        }
        move(step)
    }

    override fun goRight() {
        val step = if (snake.array[0] % column >= column - 1) {
            -(column - 1)
        } else {
            1
        }
        move(step)
    }

    override fun goDown() {
        val step = if (snake.array[0] / column >= row - 1) {
            snake.array[0] % column - snake.array[0]
        } else {
            column
        }
        move(step)
    }

    override fun goUp() {
        val step = if (snake.array[0] / column <= 0) {
            (row - 1) * column
        } else {
            -column
        }
        move(step)
    }

    private fun move(step: Int) {
        previousTail = snake.array.last()
        var pre = snake.array[0] + step
        for (i in 0 until snake.array.size) {
            val temp = snake.array[i]
            snake.array[i] = pre
            pre = temp
        }
    }

    private fun checkImpact(): Boolean {
        snake.array.forEach {
            if (it == fruit) {
                return true
            }
        }
        return false
    }

    private fun eatFruit() {
        snake.array.add(previousTail)
    }

    private fun generateFruit(): Int {
        val pool = ArrayList<Int>()
        spaceArray.forEachIndexed { index, b ->
            if (!b) pool.add(index)
        }
        return if (pool.isNotEmpty()) {
            pool[(0 until pool.size).random()]
        } else {
            //胜利
            -1
        }
    }

    interface SnakeListener {
        fun generateFruit(x: Int, y: Int) {}
        fun updateView(snakeX: Int, snakeY: Int) {}
    }

    enum class Direction {
        GO_LEFT,
        GO_RIGHT,
        GO_UP,
        GO_DOWN,
    }

}