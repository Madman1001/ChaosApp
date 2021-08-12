package com.example.view.base

import android.graphics.Canvas

abstract class BasicRender {

    private fun draw(canvas: Canvas){
        onDrawBackground(canvas)
        onDraw(canvas)
        onDrawForeground(canvas)
    }

    protected open fun onDrawBackground(canvas: Canvas){

    }

    abstract fun onDraw(canvas: Canvas)

    protected open fun onDrawForeground(canvas: Canvas){

    }
}