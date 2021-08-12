package com.example.view

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import com.example.view.snake.GreedySnakeView
import com.example.view.tetris.TetrisView

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
class GameActivity : Activity(){
    var snakeView:GreedySnakeView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        snakeView = GreedySnakeView(this,100,50)
        snakeView?.listener = SnakeScript(snakeView!!)
        findViewById<ViewGroup>(android.R.id.content).addView(snakeView)
    }
}