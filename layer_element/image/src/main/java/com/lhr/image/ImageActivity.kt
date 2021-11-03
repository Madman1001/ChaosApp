package com.lhr.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.lhr.centre.annotation.CElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "图像功能")
class ImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
    }

    fun imageBlur(mView: View){
        this.findViewById<ImageView>(R.id.image_view).apply {
            GlobalScope.launch(Dispatchers.Default){
                val option = BitmapFactory.Options()
                option.inMutable = true
                val normalBitmap =
                    BitmapFactory.decodeResource(this@ImageActivity.resources,R.drawable.imager_demo,option)
                nativeBlurBitmap(normalBitmap)
                withContext(Dispatchers.Main){
                    this@apply.setImageBitmap(normalBitmap)
                    this@apply.invalidate()
                }
            }
        }
    }

    private external fun nativeBlurBitmap(bitmap: Bitmap): Bitmap

    init {
        System.loadLibrary("chaos-image")
    }
}