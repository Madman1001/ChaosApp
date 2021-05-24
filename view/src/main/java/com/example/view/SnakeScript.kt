package com.example.view

import android.graphics.Point
import android.util.Log
import com.example.view.snake.GreedySnakeView

/**
 * @author lhr
 * @date 2021/5/10
 * @des 贪吃蛇脚本
 */
class SnakeScript(private val snake: GreedySnakeView) : GreedySnakeView.SnakeListener {
    private var fruit = Point(-1, -1)
    override fun generateFruit(x: Int, y: Int) {
        super.generateFruit(x, y)
        fruit.x = x
        fruit.y = y
        Log.e("Test","fruit $fruit")
    }

    override fun updateView(snakeX: Int, snakeY: Int) {
        super.updateView(snakeX, snakeY)
        when (snake.direction) {
            GreedySnakeView.Direction.GO_RIGHT, GreedySnakeView.Direction.GO_LEFT -> {
                if (snakeX == fruit.x) {
                    if (snakeY > fruit.y) {
                        snake.direction = GreedySnakeView.Direction.GO_UP
                    } else if (snakeY < fruit.y) {
                        snake.direction = GreedySnakeView.Direction.GO_DOWN
                    }
                }
            }
            GreedySnakeView.Direction.GO_DOWN, GreedySnakeView.Direction.GO_UP -> {
                if (snakeY == fruit.y) {
                    if (snakeX > fruit.x) {
                        snake.direction = GreedySnakeView.Direction.GO_LEFT
                    } else if (snakeX < fruit.x) {
                        snake.direction = GreedySnakeView.Direction.GO_RIGHT
                    }
                }
            }
        }

    }

}