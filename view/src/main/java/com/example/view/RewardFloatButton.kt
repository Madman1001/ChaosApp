package com.example.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.ImageView
/**
 * @author lhr
 * @date 2021/3/16 14:59
 */
@SuppressLint("UseCompatLoadingForDrawables", "AppCompatCustomView", "ClickableViewAccessibility")
class RewardFloatButton : ImageView {

    /*** 进度条总角度 */
    private var mMaxAngle = 360f

    /*** 设置起始角度，0表示由右往左顺时针旋转 */
    var mStartAngle = 0f
        set(value){
            field = value
            invalidate()
        }

    /*** 进度条最大值 */
    var maxSchedule = 5

    /*** 进度条当前值 */
    var currentSchedule = 0
        set(value) {
            field = value
            listener.onChange(value)
            invalidate()
        }

    /*** 圆环半径 */
    private var mBarRadiusSize = 0f

    /*** 进度条圆环轨迹 */
    private val mScheduleOval: RectF by lazy {
        val center = mCenterPoint
        val startX = center.x - mBarRadiusSize
        val startY = center.y - mBarRadiusSize
        val endX = center.x + mBarRadiusSize
        val endY = center.y + mBarRadiusSize
        RectF(startX, startY, endX, endY)
    }

    private val mCenterPoint: Point
        get() {
            val x = this.width / 2
            val y = this.height / 2
            return Point(x, y)
        }

    /*** 进度条所占角度 */
    private val radiusSchedule: Float
        get() = currentSchedule.toFloat() / maxSchedule.toFloat() * mMaxAngle

    /*** 画笔工具 */
    private val mBarPaint = Paint()

    /*** 状态监听器 */
    private var listener: StatusListener = object :
        StatusListener {
        override fun onClick(schedule: Int) {
        }

        override fun onChange(schedule: Int) {
        }
    }

    constructor(ctx: Context) : super(ctx) {
        mBarPaint.isAntiAlias = true
        mBarPaint.style = Paint.Style.STROKE
        mBarPaint.color = Color.YELLOW
        mBarPaint.strokeWidth = dp2px(ctx, 5f).toFloat()
        mBarRadiusSize = 0f
        mStartAngle = 0f
        mMaxAngle = 360f
    }

    constructor(ctx: Context, attr: AttributeSet) : super(ctx, attr) {
        val typedArray = ctx.obtainStyledAttributes(attr, R.styleable.RewardFloatButton)
        mBarPaint.isAntiAlias = true
        mBarPaint.style = Paint.Style.STROKE
        mBarPaint.color =
            typedArray.getColor(R.styleable.RewardFloatButton_barColor, mBarPaint.color)
        mBarPaint.strokeWidth = typedArray.getDimension(
            R.styleable.RewardFloatButton_barThick,
            dp2px(ctx, 5f).toFloat()
        )
        mBarRadiusSize = typedArray.getDimension(R.styleable.RewardFloatButton_barRadius, 0f)
        mStartAngle = typedArray.getInteger(R.styleable.RewardFloatButton_barStartAngle,0).toFloat()
        mMaxAngle = typedArray.getInteger(R.styleable.RewardFloatButton_barMaxAngle, 360).toFloat()
        typedArray.recycle()
    }

    init {
        this.setOnClickListener {}
        this.setOnTouchListener { _, _ -> false }
    }


    fun setStatusListener(listener: StatusListener) {
        this.listener = listener
    }

    fun setRingColor(ringColor: Int) {
        mBarPaint.color = right
    }

    fun setRingWidth(width: Float){
        mBarPaint.strokeWidth = width
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var mWidth = MeasureSpec.getSize(widthMeasureSpec)
        var mHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (mWidth < mBarRadiusSize * 2) {
            mWidth = (mBarRadiusSize * 2).toInt()
        }
        if (mHeight < mBarRadiusSize * 2) {
            mHeight = (mBarRadiusSize * 2).toInt()
        }

        if (mWidth < mHeight) {
            mWidth = mHeight
        } else {
            mHeight = mWidth
        }
        if (mBarRadiusSize == 0f) {
            mBarRadiusSize = mWidth.toFloat()
        }
        mBarRadiusSize -= mBarPaint.strokeWidth
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(mWidth, MeasureSpec.getMode(widthMeasureSpec))
            , MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.getMode(heightMeasureSpec))
        )
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        drawProgressBar(canvas)
        super.onDraw(canvas)
    }

    private fun drawProgressBar(canvas: Canvas?) {
        canvas?.let {
            //绘制进度条背景
            mBarPaint.alpha = 255 / 2
            it.drawArc(
                mScheduleOval,
                0f, 360f, false,
                mBarPaint
            )

            //绘制进度条
            mBarPaint.alpha = 255
            it.drawArc(
                mScheduleOval,
                mStartAngle, radiusSchedule, false,
                mBarPaint
            )
        }
    }

    private fun dp2px(ctx: Context, dp: Float): Int {
        return Math.round(
            ctx.resources.displayMetrics.density * dp
        )
    }

    private fun sp2px(ctx: Context, sp: Float): Int {
        return Math.round(
            ctx.resources.displayMetrics.scaledDensity * sp
        )
    }

    override fun setOnClickListener(l: OnClickListener?) {
        if (l != null) {
            super.setOnClickListener {
                l.onClick(it)
                listener.onClick(currentSchedule)
            }
        }
    }

    override fun toString(): String {
        return "RewardFloatButton(MAX_SCHEDULE=$mMaxAngle, mMaxRewardSchedule=$maxSchedule, mCurrentSchedule=$currentSchedule, mRadiusBarSize=$mBarRadiusSize)"
    }


    interface StatusListener {
        fun onClick(schedule: Int)
        fun onChange(schedule: Int)
    }
}