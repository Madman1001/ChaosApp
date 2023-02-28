package com.lhr.learn.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * @CreateDate: 2023/2/13
 * @Author: mac
 * @Description: 金币垂直滚动视图
 */
class GoldFlipTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    companion object {
        const val DEFAULT_ANIMATION_DURATION = 1000L
    }

    private val rv: RecyclerView
    private val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private val layoutManager: LinearLayoutManager
    private var endListener:()->Unit = {}

    @ColorInt
    private var mTextColor: Int = Color.parseColor("#ff985612")

    private var mTextSize: Float = 15f

    private var mIsFakeBoldText: Boolean = false

    val isPlaying:Boolean
        get() { return mPlayingList.any { it } }

    init {
        rv = RecyclerView(context)
        val param = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        param.gravity = Gravity.CENTER_VERTICAL or Gravity.START
        this.addView(rv, param)

        adapter = object :RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
                super.onAttachedToRecyclerView(recyclerView)
                recyclerView.itemAnimator = null
            }

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RecyclerView.ViewHolder {
                return object: RecyclerView.ViewHolder(FlipCharView(context)){}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val listText = mList.getOrNull(position) ?: emptyList()
                val fcv = holder.itemView
                if(fcv is FlipCharView){
                    fcv.setText(listText.toTypedArray(), 0)
                    fcv.paint.color = mTextColor
                    fcv.textSize = mTextSize
                    fcv.paint.isFakeBoldText = mIsFakeBoldText
                }
            }

            override fun getItemCount(): Int = mList.size
        }
        rv.adapter = adapter

        layoutManager = object: LinearLayoutManager(context){
            override fun canScrollHorizontally(): Boolean {
                return false
            }
        }.apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
        rv.layoutManager = layoutManager
    }

    private var mText = ""
    private var mList = listOf<List<String>>()
    private var mPlayingList = mutableListOf<Boolean>()

    @SuppressLint("NotifyDataSetChanged")
    fun setText(text: String){
        mText = text

        val textList = mutableListOf<List<String>>()
        for (c in text.toCharArray()) {
            textList.add(listOf(c.toString()))
        }
        mList = textList
        mPlayingList = MutableList<Boolean>(textList.size) { false }
        adapter.notifyDataSetChanged()
    }

    fun getText(): String = mText

    @SuppressLint("NotifyDataSetChanged")
    fun setTextColor(@ColorInt textColor: Int){
        mTextColor = textColor
        adapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTextSize(textSize: Float){
        mTextSize = textSize
        adapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun isBold(isBold: Boolean){
        mIsFakeBoldText = isBold
        adapter.notifyDataSetChanged()
    }

    fun setAndPlayAnimation(animationString: List<List<String>>, duration: Long = DEFAULT_ANIMATION_DURATION){
        if (isPlaying) return
        val playingList = mPlayingList

        val sb = StringBuilder()
        for (list in animationString) {
            sb.append(list.getOrNull(0) ?: "")
        }

        if (playingList.isNotEmpty()) {
            playingList[0] = true
        }

        setText(sb.toString())

        rv.post {
            val size = animationString.size
            for (b in 0 until playingList.size) {
                playingList[b] = false
            }
            for (i in 0 until size) {
                if (playingList.size > i) {
                    playingList[i] = false
                }
                val holder = rv.findViewHolderForAdapterPosition(i) ?: continue
                val list = animationString.getOrNull(i) ?: continue
                val itemView = holder.itemView
                if (itemView is FlipCharView) {
                    itemView.setAnimationEndListener {
                        itemView.setAnimationEndListener {}
                        if (playingList.size > i) {
                            playingList[i] = false
                        }
                        if (playingList.all { a -> !a }) {
                            endListener.invoke()
                        }
                    }
                    if (playingList.size > i) {
                        playingList[i] = true
                    }
                    itemView.startAnimationAndSetText(list.toTypedArray(), duration)
                }
            }
        }
    }

    fun setAnimationEndListener(listener: () -> Unit){
        this.endListener = listener
    }
}