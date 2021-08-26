package com.lhr.view.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.lhr.view.CalculateUtils

/**
 * @author lhr
 * @date 2021/8/23
 * @des 视频文件查看View
 */
class MediaCheckView(context: Context): View(context) {

    private val _MetadataReriver = MediaMetadataRetriever()

    var videoPath = ""
        set(value) {
            _MetadataReriver.setDataSource(value)
        }

    /**
     * 测量
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * 布局
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    /**
     * 绘制
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }



}