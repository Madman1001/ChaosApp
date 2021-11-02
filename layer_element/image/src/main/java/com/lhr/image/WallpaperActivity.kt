package com.lhr.image

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lhr.centre.annotation.CElement

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "图像功能")
class WallpaperActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sayHello()
    }

    private external fun sayHello()

    init {
        System.loadLibrary("chaos-image")
    }
}