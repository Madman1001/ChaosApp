package com.lhr.image

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.RENDERMODE_CONTINUOUSLY
import android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.lhr.centre.annotation.CElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.microedition.khronos.opengles.GL

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "εΎεεθ½")
class ImageActivity : AppCompatActivity() {
    private var glView: GLSurfaceView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

    }

    override fun onResume() {
        super.onResume()
        glView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        glView?.onPause()
    }

    fun gotoWallpaper(view: View){
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            this.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(this@ImageActivity, GameWallpaperService::class.java))
        }
        startActivity(intent)

    }

    fun imageNormal(view: View){
        GlobalScope.launch(Dispatchers.Default){
            val option = BitmapFactory.Options()
            option.inMutable = true
            val normalBitmap =
                BitmapFactory.decodeResource(this@ImageActivity.resources,R.drawable.imager_demo,option)
            withContext(Dispatchers.Main){
                if (glView?.parent != null){
                    glView?.parent?.run {
                        (this as ViewGroup).removeView(glView)
                    }
                }
                glView = GLSurfaceView(this@ImageActivity)
                glView?.setEGLContextClientVersion(2)
                glView?.setRenderer(GLImageRender(this@ImageActivity, normalBitmap))
                this@ImageActivity.findViewById<ViewGroup>(R.id.image_content_layout).addView(glView)
            }
        }
    }

    fun imageBlur(view: View){
        GlobalScope.launch(Dispatchers.Default){
            val option = BitmapFactory.Options()
            option.inMutable = true
            val normalBitmap =
                BitmapFactory.decodeResource(this@ImageActivity.resources,R.drawable.imager_demo,option)
            withContext(Dispatchers.Main){
                if (glView?.parent != null){
                    glView?.parent?.run {
                        (this as ViewGroup).removeView(glView)
                    }
                }
                glView = GLSurfaceView(this@ImageActivity)
                glView?.setEGLContextClientVersion(2)
                glView?.setRenderer(GLBlurImageRender(this@ImageActivity, normalBitmap))
                this@ImageActivity.findViewById<ViewGroup>(R.id.image_content_layout).addView(glView)
            }
        }
    }

    fun digitalRain(view: View){
        if (glView?.parent != null){
            glView?.parent?.run {
                (this as ViewGroup).removeView(glView)
            }
        }
        glView = GLSurfaceView(this@ImageActivity)
        glView?.setEGLContextClientVersion(2)
        glView?.setRenderer(GLDigitalRainRender(this@ImageActivity))
        this@ImageActivity.findViewById<ViewGroup>(R.id.image_content_layout).addView(glView)
    }
}