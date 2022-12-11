package com.lhr.wallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.View
import android.view.ViewGroup
import com.lhr.centre.annotation.CElement
import com.lhr.common.ui.BaseActivity
import com.lhr.image.databinding.ActivityImageBinding
import com.lhr.wallpaper.base.BitmapRenderer
import com.lhr.wallpaper.base.GLSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.IntBuffer

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "图像功能")
class ImageActivity : BaseActivity<ActivityImageBinding>() {
    private var glView: GLSurfaceView? = null

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
                glView = GLSurfaceView(this@ImageActivity).apply {
                    setEGLContextClientVersion(2)
                    setRenderer(GLImageRender(this@ImageActivity, normalBitmap))
                }
                mBinding.imageContentLayout.addView(glView, 0)

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
                mBinding.imageContentLayout.addView(glView, 0)
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
        mBinding.imageContentLayout.addView(glView, 0)
    }


    fun obtainBitmap3(src: Bitmap, result: (Bitmap)->Unit){
        val width = src.width
        val height = src.height
        val glRenderer = BitmapRenderer(this.application, src)
        val glPbufferSurface = GLSurface(width, height)
        glRenderer.addSurface(glPbufferSurface)
        glRenderer.startRender()
        glRenderer.requestRender()
        glRenderer.postRunnable {
            val ib: IntBuffer = IntBuffer.allocate(width * height)
            GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib)

            val bitmap: Bitmap = frameToBitmap(width, height, ib)
            result.invoke(bitmap)
        }
    }

    /**
     * 将数据转换成bitmap(OpenGL和Android的Bitmap色彩空间不一致，这里需要做转换)
     *
     * @param width 图像宽度
     * @param height 图像高度
     * @param ib 图像数据
     * @return bitmap
     */
    private fun frameToBitmap(width: Int, height: Int, ib: IntBuffer): Bitmap {
        val pixs = ib.array()
        // 扫描转置(OpenGl:左上->右下 Bitmap:左下->右上)
        for (y in 0 until height / 2) {
            for (x in 0 until width) {
                val pos1 = y * width + x
                val pos2 = (height - 1 - y) * width + x
                val tmp = pixs[pos1]
                pixs[pos1] =
                    pixs[pos2] and -0xff0100 or (pixs[pos2] shr 16 and 0xff) or (pixs[pos2] shl 16 and 0x00ff0000) // ABGR->ARGB
                pixs[pos2] =
                    tmp and -0xff0100 or (tmp shr 16 and 0xff) or (tmp shl 16 and 0x00ff0000)
            }
        }
        if (height % 2 == 1) { // 中间一行
            for (x in 0 until width) {
                val pos = (height / 2 + 1) * width + x
                pixs[pos] =
                    pixs[pos] and -0xff0100 or (pixs[pos] shr 16 and 0xff) or (pixs[pos] shl 16 and 0x00ff0000)
            }
        }
        return Bitmap.createBitmap(pixs, width, height, Bitmap.Config.ARGB_8888)
    }
}