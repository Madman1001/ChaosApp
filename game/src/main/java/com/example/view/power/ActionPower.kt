package com.example.view.power

import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * @author lhr
 * @date 2021/5/17
 * @des
 */
object ActionPower {
    private val mMainHandler = Handler(Looper.getMainLooper())

    fun startRun(gap: Long, action: () -> Unit) {
        if (gap > 0) {
            ActionRunnable(gap,action).run()
        }
    }

    fun stopRun(){
        mMainHandler.removeCallbacksAndMessages(null)
    }

    private class ActionRunnable(private val delayTime: Long,private val action: () -> Unit) : Runnable {
        override fun run() {
            action.invoke()
            mMainHandler.postDelayed(this,delayTime)
        }
    }
}