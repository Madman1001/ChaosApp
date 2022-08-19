package com.lhr.learn.bitmap

import android.os.Bundle
import android.view.View
import com.lhr.learn.R
import com.lhr.common.ui.BaseFragment
import com.lhr.common.utils.BitmapUtils
import com.lhr.learn.databinding.FragmentCropImageBinding

/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
class BitmapCropFragment: BaseFragment<FragmentCropImageBinding>() {
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.bitmapCropView.run {
            isEnabled = true
            mResponseSpeed = 10
            addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(v: View) {
                }

                override fun onViewAttachedToWindow(v: View) {
                    bitmap = BitmapUtils.getBitmapFromResource(v.resources,R.drawable.pixel_forest)
                }
            })
        }
    }
}