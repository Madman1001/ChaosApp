package com.lhr.learn

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lhr.centre.annotation.CElement
import com.lhr.learn.view.BitmapCropView
import com.lhr.utils.BitmapUtils

/**
 * @author lhr
 * @date 2021/4/27
 * @des
 */
@CElement(name = "Android基础")
class LearnActivity : AppCompatActivity(){
    private val tag = "AS_${this::class.java.simpleName}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn)
        showCropView()
    }

    private fun showCropView(){
        val cropView = this.findViewById<BitmapCropView>(R.id.bitmapCropView)
        cropView.isEnabled = true
        cropView.mResponseSpeed = 10
        cropView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View) {

            }

            override fun onViewAttachedToWindow(v: View) {
                cropView.bitmap = BitmapUtils.getBitmapFromResource(v.resources,R.drawable.pixel_forest)
            }
        })
    }
}