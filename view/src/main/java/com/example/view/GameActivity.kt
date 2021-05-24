package com.example.view

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import com.example.view.base.BaseMap
import com.example.view.snake.GreedySnakeView
import com.example.view.tetris.TetrisView

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
class GameActivity : Activity(), View.OnClickListener{
    var snakeView:GreedySnakeView? = null
    var tetrisView:TetrisView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        snakeView = GreedySnakeView(this)
        snakeView?.listener = SnakeScript(snakeView!!)
        findViewById<ViewGroup>(android.R.id.content).addView(snakeView)
//        tetrisView = TetrisView(this)
//        findViewById<ViewGroup>(android.R.id.content).addView(tetrisView)

//        setContentView(R.layout.game_activity)
//        findViewById<View>(R.id.snake_left).setOnClickListener(this)
//        findViewById<View>(R.id.snake_right).setOnClickListener(this)
//        findViewById<View>(R.id.snake_up).setOnClickListener(this)
//        findViewById<View>(R.id.snake_down).setOnClickListener(this)
//        findViewById<ImageView>(R.id.snake_auto).setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.snake_left ->{
                tetrisView?.goLeft()
                snakeView?.direction = GreedySnakeView.Direction.GO_LEFT
            }
            R.id.snake_right ->{
                tetrisView?.goRight()
                snakeView?.direction = GreedySnakeView.Direction.GO_RIGHT
            }
            R.id.snake_up ->{
                tetrisView?.goUp()
                snakeView?.direction = GreedySnakeView.Direction.GO_UP
            }
            R.id.snake_down ->{
                tetrisView?.goDown()
                snakeView?.direction = GreedySnakeView.Direction.GO_DOWN
            }
            R.id.snake_auto ->{
                if (snakeView?.listener != null){
                    snakeView?.listener = null
                    (v as ImageView).setBackgroundResource(R.drawable.ic_auto_false)
                }else{
                    snakeView?.listener = SnakeScript(snakeView!!)
                    (v as ImageView).setBackgroundResource(R.drawable.ic_auto_true)
                }
            }
        }
    }
}