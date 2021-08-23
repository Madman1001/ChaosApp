package com.lhr.game

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import com.lhr.game.view.GameView

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
class GameActivity : Activity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<ViewGroup>(android.R.id.content).addView(GameView(this))
    }
}