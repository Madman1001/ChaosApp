package com.lhr.wallpaper

import android.graphics.Color
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class GameWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return GameWallEngine()
    }

    private inner class GameWallEngine : WallpaperService.Engine() {
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
            val canvas = holder?.lockCanvas()
            canvas?.drawColor(Color.BLUE)
            holder?.unlockCanvasAndPost(canvas)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
        }
    }
}