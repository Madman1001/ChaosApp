package com.lhr.view

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import android.widget.Button
import com.lhr.centre.annotation.CElement
import com.lhr.view.bitmap.BitmapCropView
import com.lhr.view.utils.BitmapUtils
import com.lhr.view.utils.MediaUtils
import com.lhr.view.utils.GLESUtils

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "视图功能")
class ViewActivity : Activity(),SurfaceHolder.Callback {
    private var mediaPlayer:MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)
        val surfaceView = findViewById<SurfaceView>(R.id.surface_view)
        surfaceView.holder.addCallback(this)
    }

    private fun showCropView(){
        val cropView = BitmapCropView(this)
        cropView.isEnabled = true
        cropView.mResponseSpeed = 10
        cropView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View) {

            }

            override fun onViewAttachedToWindow(v: View) {
                cropView.bitmap = BitmapUtils.getBitmapFromResource(v.resources,R.drawable.pixel_forest)
            }
        })
        findViewById<ViewGroup>(android.R.id.content).addView(cropView)
    }

    private fun showMediaFrame(){
        val cropView = BitmapCropView(this)
        cropView.isEnabled = true
        cropView.mResponseSpeed = 10
        cropView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View) {

            }

            override fun onViewAttachedToWindow(v: View) {
                val am = v.context.applicationContext.assets
                val afd = am.openFd("video_heart.mp4")
                cropView.bitmap = MediaUtils.getMediaFrame(afd.fileDescriptor,afd.startOffset,afd.length)
                afd.close()
            }
        })
        findViewById<ViewGroup>(android.R.id.content).addView(cropView)
    }

    private fun showSurfaceMedia(surface: Surface){
        if (mediaPlayer != null){
            mediaPlayer?.release()
            mediaPlayer = null
        }
        val am = this.assets
        val afd = am.openFd("video_heart.mp4")
        val mp = MediaPlayer()
        mp.setDataSource(afd.fileDescriptor,afd.startOffset,afd.length)
        mp.setSurface(surface)
        mp.isLooping = true
        mp.setOnPreparedListener {
            mediaPlayer?.release()
            mediaPlayer = it
            mediaPlayer?.start()

        }
        mp.prepareAsync()
    }

    private fun showSurfaceBitmap(surface: Surface){
        val bitmap = BitmapUtils.getBitmapFromResource(this.resources,R.drawable.pixel_forest)
        if (bitmap != null){
            val view = findViewById<SurfaceView>(R.id.surface_view)
            GLESUtils.drawBitmapToSurface(surface,bitmap,view.width,view.height)
//            val canvas = surface.lockCanvas(null)
//            canvas.drawBitmap(bitmap,0f,0f,null)
//            surface.unlockCanvasAndPost(canvas)
//            if (SysReflection.runSysMethod(Surface::class.java,surface,"forceScopedDisconnect")){
//                Toast.makeText(this,"反射成功",Toast.LENGTH_SHORT).show()
//            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val param = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        val btn1 = Button(this)
        btn1.text = "展示图片"
        val btn2 = Button(this)
        btn2.text = "播放视频"
        findViewById<ViewGroup>(android.R.id.content).addView(btn1,param)
        btn1.setOnClickListener {

            showSurfaceBitmap(holder.surface)
        }
        btn1.y += 100

        findViewById<ViewGroup>(android.R.id.content).addView(btn2,param)
        btn2.setOnClickListener {
            showSurfaceMedia(holder.surface)
        }
        btn2.y += 300

        val btn3 = Button(this)
        btn3.text = "清除画面"
        findViewById<ViewGroup>(android.R.id.content).addView(btn3,param)
        btn3.setOnClickListener {
            mediaPlayer?.release()
            mediaPlayer = null
            GLESUtils.clearSurfaceBuffer(holder.surface)
        }
        btn3.y += 500
    }
}