package com.lhr.learn.bitmap

import android.os.Bundle
import android.view.View
import com.lhr.learn.R
import com.lhr.learn.base.BaseFragment
import com.lhr.utils.BitmapUtils

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
class BitmapCropFragment: BaseFragment() {
    override fun getLayout(): Int = R.layout.fragment_crop_image

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showCropView()
    }

    private fun showCropView(){
        val cropView = view?.findViewById<BitmapCropView>(R.id.bitmapCropView) ?: return
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