package com.example.game

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
class GameActivity : Activity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val snakeView = GreedySnakeView(this,100,50)
//        snakeView?.listener = SnakeScript(snakeView!!)
//        findViewById<ViewGroup>(android.R.id.content).addView(snakeView)

        findViewById<ViewGroup>(android.R.id.content).addView(DemoView(this))
    }
}