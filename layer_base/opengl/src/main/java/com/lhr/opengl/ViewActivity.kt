package com.lhr.opengl

import android.app.Activity
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.os.Bundle
import android.view.*
import androidx.core.graphics.applyCanvas
import androidx.core.view.drawToBitmap
import com.lhr.centre.annotation.CElement
import com.lhr.opengl.utils.GLESUtils

/**
 * @author lhr
 * @date 2021/5/7
 * @des
 */
@CElement(name = "视图功能")
class ViewActivity : Activity(), SurfaceHolder.Callback {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)
        val surfaceView = findViewById<SurfaceView>(R.id.surface_view)
        surfaceView.holder.addCallback(this)
    }

    private fun showSurfaceMedia(surface: Surface) {
        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
        }
        val am = this.assets
        val afd = am.openFd("video_heart.mp4")
        val mp = MediaPlayer()
        mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        mp.setSurface(surface)
        mp.isLooping = true
        mp.setOnPreparedListener {
            mediaPlayer?.release()
            mediaPlayer = it
            mediaPlayer?.start()

        }
        mp.prepareAsync()
    }

    private fun showSurfaceBitmap(surface: Surface) {
        val bitmap = this.window.decorView
            .drawToBitmap()
            .applyCanvas {
                val paint = Paint()
                paint.color = Color.parseColor("#AAAAAA")
                this.drawCircle(this.width / 2f, this.height / 2f, this.width / 4f, paint)
            }
        val view = findViewById<SurfaceView>(R.id.surface_view)
        GLESUtils.drawBitmapToSurface(surface, bitmap, view.width, view.height)
    }

    private fun clearSurfaceBitmap(surface: Surface) {
        mediaPlayer?.release()
        mediaPlayer = null
        GLESUtils.clearSurfaceBuffer(surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        findViewById<View>(R.id.btn01).setOnClickListener {
            showSurfaceBitmap(holder.surface)
        }

        findViewById<View>(R.id.btn02).setOnClickListener {
            showSurfaceMedia(holder.surface)
        }

        findViewById<View>(R.id.btn03).setOnClickListener {
            clearSurfaceBitmap(holder.surface)
        }
    }
}