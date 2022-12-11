package com.lhr.learn.bitmap.gl

import android.opengl.EGL14
import android.view.Surface
import androidx.annotation.IntRange


/**
 * @author lhr
 * @date 7/12/2022
 * @des
 */
class GLSurface {
    companion object{
        const val TYPE_WINDOW_SURFACE = 0
        const val TYPE_PBUFFER_SURFACE = 1
        const val TYPE_PIXMAP_SURFACE = 2
    }
    class Viewport {
        var x = 0
        var y = 0
        var width = 0
        var height = 0
    }

    @IntRange(from = TYPE_WINDOW_SURFACE.toLong(), to = TYPE_PIXMAP_SURFACE.toLong())
    val type: Int
    var surface: Any? = null
    var eglSurface = EGL14.EGL_NO_SURFACE
    var viewport = Viewport()

    constructor(width: Int, height: Int) {
        setViewport(0, 0, width, height)
        surface = null
        type = TYPE_PBUFFER_SURFACE
    }
    constructor(surface: Surface, width: Int, height: Int): this(surface, 0, 0, width, height)
    constructor(surface: Surface, x: Int, y: Int, width: Int, height: Int) {
        setViewport(x, y, width, height)
        this.surface = surface
        type = TYPE_WINDOW_SURFACE
    }

    fun setViewport(x: Int, y: Int, width: Int, height: Int) {
        viewport.x = x
        viewport.y = y
        viewport.width = width
        viewport.height = height
    }
}