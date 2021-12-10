package com.lhr.game

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import com.lhr.centre.annotation.CElement
import com.lhr.game.snake.GreedySnakeView

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "游戏功能")
class GameActivity : Activity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val snakeView = GreedySnakeView(this, 40, 20)
        snakeView.listener = SnakeScript(snakeView)
        findViewById<ViewGroup>(android.R.id.content).addView(snakeView)

//        findViewById<ViewGroup>(android.R.id.content).addView(DemoView(this))
    }
}