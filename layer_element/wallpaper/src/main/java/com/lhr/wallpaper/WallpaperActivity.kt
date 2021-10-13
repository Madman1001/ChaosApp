package com.lhr.wallpaper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.lhr.centre.annotation.CElement

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "壁纸功能")
class WallpaperActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sayHello(getNativeString())
    }

    external fun getNativeString(): String

    fun sayHello(message: String){
        Log.e("WallpaperActivity",message)
    }

    init {
        System.loadLibrary("wallpaper-image")
    }
}