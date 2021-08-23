package com.example.view.tetris

import android.content.Context
import android.view.View
import com.example.view.base.BaseMap
import com.example.view.input.IActionInput
import com.example.view.power.ActionPower

/**
 * @author lhr
 * @date 2021/5/11
 * @des 俄罗斯方块
 */
class TetrisView(context: Context, row: Int = 20, column: Int = 10) : BaseMap(context, row, column),
    IActionInput {

    private var index = 0

    private var currentComponent = TetrisComponent(-2 * column, -2 * column + 1, -1 * column, -1 * column + 1)
    private var bottomIndex = 0
    init {
        this.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View) {
                ActionPower.stopRun()
            }

            override fun onViewAttachedToWindow(v: View) {
                ActionPower.startRun(500L, this@TetrisView::updateView)
            }
        })
    }

    private fun updateView() {
        if (bottomIndex < row) {
            clearMap()
            moveDown()
        }
        invalidate()
    }

    override fun goLeft() {
        spaceArray[index] = false
        index--
        spaceArray[index] = true
    }

    override fun goRight() {
        spaceArray[index] = false
        index++
        spaceArray[index] = true
    }

    override fun goUp() {
        spaceArray[index] = false
        index -= column
        spaceArray[index] = true
    }

    override fun goDown() {
        spaceArray[index] = false
        index += column
        spaceArray[index] = true
    }

    private fun moveUp(){
        //todo 变化形态
    }

    private fun moveDown() {
        for (i in currentComponent.array.indices) {
            currentComponent.array[i] += column

            val index = currentComponent.array[i]

            if (index >= 0 && index < row * column){
                spaceArray[index] = true
            }
        }
        bottomIndex++
    }

    private fun moveLeft(){
        if (currentComponent.array.all { it / column > 0 }) {
            for (i in currentComponent.array.indices) {
                currentComponent.array[i] -= 1

                val index = currentComponent.array[i]

                if (index >= 0 && index < row * column) {
                    spaceArray[index] = true
                }
            }
        }
    }

    private fun moveRight(){
        if (currentComponent.array.all { it / column < column-1 }) {
            for (i in currentComponent.array.indices) {
                currentComponent.array[i] += 1

                val index = currentComponent.array[i]

                if (index >= 0 && index < row * column) {
                    spaceArray[index] = true
                }
            }
        }
    }
}