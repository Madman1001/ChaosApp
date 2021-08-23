package com.lhr.view

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.lhr.view.bitmap.BitmapCropView

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
class ViewActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cropView = BitmapCropView(this)
        cropView.isEnabled = true
        cropView.mResponseSpeed = 10
        cropView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View) {

            }

            override fun onViewAttachedToWindow(v: View) {
                cropView.bitmap = BitmapUtils.getBitmapFromResource(
                    v.context.resources,
                    R.drawable.pixel_forest,
                    v.width,
                    v.height
                )
            }
        })
        findViewById<ViewGroup>(android.R.id.content).addView(cropView)
    }
}