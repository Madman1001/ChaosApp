package com.lhr.learn.bitmap

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.lhr.common.ui.BaseFragment
import com.lhr.common.utils.BitmapUtils
import com.lhr.learn.R
import com.lhr.learn.databinding.FragmentCropImageBinding
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


/**
 * @CreateDate: 2022/6/27
 * @Author: mac
 * @Description:
 */
class BitmapCropFragment: BaseFragment<FragmentCropImageBinding>() {
    override fun initView(savedInstanceState: Bundle?) {
        mBinding.click = this
    }

    fun obtain1(){
        lifecycleScope.launch {
            withContext(IO){
                val time = System.currentTimeMillis()
                val bitmap = BitmapUtils.getBitmapFromResource(attachActivity.resources,R.drawable.pixel_forest) ?: return@withContext
                val bBitmap = obtainBitmap(bitmap)
                withContext(Main){
                    mBinding.imageParallelView.setupParallelBitmap(bitmap, bBitmap)
                    Log.e("TAG", "obtain1 ${System.currentTimeMillis() - time}")
                }
            }
        }
    }

    fun obtainBitmap(src: Bitmap): Bitmap{
        val random = Random.nextInt()
        val colors = IntArray(src.width * src.height)
        src.getPixels(colors, 0, src.width, 0, 0, src.width, src.height)
        for (i in colors.indices) {
            colors[i] = colors[i] and random
        }
        return Bitmap.createBitmap(colors, src.width, src.height, src.config)
    }
}