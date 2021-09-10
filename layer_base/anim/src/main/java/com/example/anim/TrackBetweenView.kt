package com.example.anim

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout

/**
 * @author lhr
 * @date 2021/9/10
 * @des 简易的轨迹滑动组件，现只支持两个滑块
 */
class TrackBetweenView : FrameLayout {
    var currentSelection: Select
        private set

    private val trackRight: ViewGroup
    private val trackLeft: ViewGroup

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_between_track,this,true)

        trackRight = this.findViewById<ViewGroup>(R.id.track_two)
        trackLeft = this.findViewById<ViewGroup>(R.id.track_one)

        currentSelection = Select.LEFT
    }

    fun select(selection: Select){
        if (currentSelection == selection){
            return
        }

        when(selection){
            Select.LEFT -> {
                playLeftAnimation(600L,true)
                playRightAnimation(600L,false)
            }
            Select.RIGHT -> {
                playLeftAnimation(600L,false)
                playRightAnimation(600L,true)
            }
        }
        currentSelection = selection
    }

    private fun playLeftAnimation(duration: Long, isSelect: Boolean){
        val frontAnimationSet =
            generateAnimationSet((trackRight.width * 1.0f) / (trackLeft.width * 1.0f),!isSelect)
        val backAnimationSet =
            generateAnimationSet((trackRight.width * 1.0f) / (trackLeft.width * 1.0f),isSelect)
        frontAnimationSet.duration = duration
        backAnimationSet.duration = duration
        this.findViewById<View>(R.id.guide_one_back_iv).startAnimation(backAnimationSet)
        this.findViewById<View>(R.id.guide_one_front_iv).startAnimation(frontAnimationSet)
    }

    private fun playRightAnimation(duration: Long, isSelect: Boolean){
        val frontAnimationSet =
            generateAnimationSet((trackLeft.width * 1.0f) / (trackRight.width * 1.0f),!isSelect)
        val backAnimationSet =
            generateAnimationSet((trackLeft.width * 1.0f) / (trackRight.width * 1.0f),isSelect)
        frontAnimationSet.duration = duration
        backAnimationSet.duration = duration
        this.findViewById<View>(R.id.guide_two_back_iv).startAnimation(backAnimationSet)
        this.findViewById<View>(R.id.guide_two_front_iv).startAnimation(frontAnimationSet)
    }

    /**
     * 生成拉伸动画，只限x轴
     */
    private fun generateAnimationSet(toScale: Float,isTransparent: Boolean): AnimationSet{
        return AnimationSet(true).apply {
            val scale = ScaleAnimation(
                1f,toScale,
                1f, 1f,
                Animation.RELATIVE_TO_SELF,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f)
            val alpha = if (isTransparent){
                AlphaAnimation(1f,0f)
            }else{
                AlphaAnimation(0f,1f)
            }
            this.addAnimation(scale)
            this.addAnimation(alpha)
            this.isFillEnabled = true
            this.fillAfter = true
        }
    }

    enum class Select{
        LEFT, //左边
        RIGHT //右边
    }
}