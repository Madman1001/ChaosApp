package com.lhr.wallpaper

import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class GameWallpaperService : WallpaperService() {
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreateEngine(): Engine {
        return GameWallEngine()
    }

    private inner class GameWallEngine : WallpaperService.Engine() {
        private val runnable = object : Runnable{
            override fun run() {
                val canvas = this@GameWallEngine.surfaceHolder.lockCanvas()
                val red = (0..255).random()
                val green = (0..255).random()
                val blue = (0..255).random()
                canvas?.drawRGB(red,green,blue)
                this@GameWallEngine.surfaceHolder.unlockCanvasAndPost(canvas)
                mainHandler.postDelayed(this,5000L)
            }
        }
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible){
                runnable.run()
            }else{
                mainHandler.removeCallbacksAndMessages(null)
            }
        }

        override fun getSurfaceHolder(): SurfaceHolder {
            return super.getSurfaceHolder()
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
        }

        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            mainHandler.removeCallbacksAndMessages(null)
        }
    }
}