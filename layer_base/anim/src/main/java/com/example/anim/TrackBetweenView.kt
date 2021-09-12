package com.example.anim

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * @author lhr
 * @date 2021/9/10
 * @des 简易的轨迹滑动组件，现只支持两个滑块
 */
class TrackBetweenView : FrameLayout {
    var currentSelection: Select = Select.UNKNOWN
        private set

    var animDuration = 600L

    private val trackRight: ViewGroup
    private val trackLeft: ViewGroup

    /**
     * 选中时的长度，和取消选择时的长度
     */
    private var selectLen = 0f
    private var unselectLen = 0f

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_between_track,this,true)

        trackLeft = this.findViewById<ViewGroup>(R.id.track_one)
        trackRight = this.findViewById<ViewGroup>(R.id.track_two)
        this.post {
            val w = this.findViewById<View>(R.id.guide_track)
            selectLen = (w.width * 1.0f) / 7f * 4f
            unselectLen = (w.width * 1.0f) / 7f * 2f
            select(Select.LEFT)
        }
    }

    fun select(selection: Select){
        if (currentSelection == selection){
            return
        }
        playLeftAnimation(animDuration,selection == Select.LEFT)
        playRightAnimation(animDuration,selection == Select.RIGHT)
        currentSelection = selection
    }

    private fun playLeftAnimation(duration: Long, isSelect: Boolean){
        this.findViewById<View>(R.id.guide_one_front_iv).apply {
            this.animate()
                .alpha(if (isSelect) 1f else 0f)
                .setDuration(duration)
                .start()
        }
        this.findViewById<View>(R.id.guide_one_back_iv).apply {
            this.animate()
                .alpha(if (isSelect) 0f else 1f)
                .setDuration(duration)
                .start()
        }
        trackLeft.apply {
            this.animate()
                .scaleX(if (isSelect) selectLen / this.width else unselectLen / this.width)
                .setDuration(duration)
                .start()
        }
    }

    private fun playRightAnimation(duration: Long, isSelect: Boolean){
        this.findViewById<View>(R.id.guide_two_front_iv).apply {
            this.animate()
                .alpha(if (isSelect) 1f else 0f)
                .setDuration(duration)
                .start()
        }
        this.findViewById<View>(R.id.guide_two_back_iv).apply {
            this.animate()
                .alpha(if (isSelect) 0f else 1f)
                .setDuration(duration)
                .start()
        }
        trackRight.apply {
            this.animate()
                .scaleX(if (isSelect) selectLen / this.width else unselectLen / this.width)
                .setDuration(duration)
                .start()
        }
    }

    enum class Select{
        LEFT, //左边
        RIGHT, //右边
        UNKNOWN
    }
}