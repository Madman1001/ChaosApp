package com.lhr.game.snake

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import com.lhr.game.base.BaseMap
import com.lhr.game.input.IActionInput
import com.lhr.game.power.ActionPower

/**
 * @author lhr
 * @date 2021/5/7
 * @des 贪吃蛇
 */
@SuppressLint("ViewConstructor")
class GreedySnakeView(context: Context, row: Int = 20, column: Int = 10) : BaseMap(context,row, column), IActionInput {
    private var snake = GreedySnakeComponent((0 .. row*column).random())
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
            return
        }
        when (direction) {
            Direction.GO_LEFT -> goLeft()
            Direction.GO_RIGHT -> goRight()
            Direction.GO_UP -> goUp()
            Direction.GO_DOWN -> goDown()
        }
        listener?.updateView(snake.getHead() % column,snake.getHead() / column)

        if (checkImpactFruit()) {
            //碰撞到果实
            eatFruit()
            fruit = generateFruit()
        }

        spaceArray.fill(false)
        if (checkImpactSelf()){
            //碰撞到自身
            fruit = generateFruit()
            snake = GreedySnakeComponent((0 until row*column).random())
        }

        for (i in 0 until snake.getLength()){
            spaceArray[snake.getNode(i)] = true
        }

        if (fruit in spaceArray.indices) {
            spaceArray[fruit] = true
        }

        invalidate()
    }

    override fun goLeft() {
        val step = if (snake.getHead() % column <= 0) {
            column - 1
        } else {
            -1
        }
        move(step)
    }

    override fun goRight() {
        val step = if (snake.getHead() % column >= column - 1) {
            -(column - 1)
        } else {
            1
        }
        move(step)
    }

    override fun goDown() {
        val step = if (snake.getHead() / column >= row - 1) {
            snake.getHead() % column - snake.getHead()
        } else {
            column
        }
        move(step)
    }

    override fun goUp() {
        val step = if (snake.getHead() / column <= 0) {
            (row - 1) * column
        } else {
            -column
        }
        move(step)
    }

    private fun move(step: Int) {
        previousTail = snake.getTail()
        var pre = snake.getHead() + step
        for (i in 0 until snake.getLength()) {
            val temp = snake.getNode(i)
            snake.setNode(i,pre)
            pre = temp
        }
    }

    private fun checkImpactSelf(): Boolean {
        if (snake.getLength() >= 5){
            val head = snake.getHead()
            for (i in 1 until snake.getLength())
                if (head == snake.getNode(i)){
                    return true
                }
        }

        return false
    }
    
    private fun checkImpactFruit(): Boolean {
        for (i in 0 until snake.getLength()){
            if (snake.getNode(i) == fruit) {
                return true
            }
        }
        return false
    }

    private fun eatFruit() {
        snake.addBody(previousTail)
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