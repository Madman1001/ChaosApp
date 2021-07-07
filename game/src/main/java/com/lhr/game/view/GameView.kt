package com.lhr.game.view

import android.content.Context
import android.graphics.Canvas
import android.view.View
import com.lhr.game.base.BaseGameView
import com.lhr.game.snake.GreedySnakeView

class GameView(context: Context) : View(context) {
    private var gameView: BaseGameView? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (gameView == null){
            gameView = GreedySnakeView(canvas!!)
        }
        gameView?.draw(canvas!!)
    }
}