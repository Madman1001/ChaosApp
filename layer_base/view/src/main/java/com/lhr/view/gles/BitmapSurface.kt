package com.lhr.view.gles

import android.opengl.*
import android.os.Build
import android.view.Surface
import androidx.annotation.RequiresApi
import com.lhr.view.render.TextureRender

/**
 * @author lhr
 * @date 2021/8/27
 * @des
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class BitmapSurface(private val surface: Surface, val width: Int, val height: Int) {

    val eglDisplay: EGLDisplay
    val eglConfig: EGLConfig
    val eglSurface: EGLSurface
    val eglContext: EGLContext

    init {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        eglConfig = makeEGLConfig()
        eglContext = makeEGLContext(eglConfig)
        eglSurface = makeEGLSurface(eglDisplay, eglConfig, eglContext, surface)
    }

    private fun makeEGLConfig(): EGLConfig {
        var renderableType = EGL14.EGL_OPENGL_ES2_BIT
        //  GLES 3.0
        //  renderableType = renderableType or EGLExt.EGL_OPENGL_ES3_BIT_KHR
        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            //EGL14.EGL_DEPTH_SIZE, 16,
            //EGL14.EGL_STENCIL_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, renderableType,
            EGL14.EGL_NONE, 0,
            EGL14.EGL_NONE
        )
        val configs =
            arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, configs.size, numConfigs, 0)
        return configs[0]!!
    }

    private fun makeEGLContext(eglConfig: EGLConfig): EGLContext {
        val attrib2_list = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        val mEGLContext: EGLContext = EGL14.eglCreateContext(
            eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT,
            attrib2_list, 0
        )
        return mEGLContext
    }

    /**
     * 设置surface为gles当前操作对象
     */
    private fun makeEGLSurface(
        eglDisplay: EGLDisplay,
        eglConfig: EGLConfig,
        eglContext: EGLContext,
        surface: Surface
    ): EGLSurface {
        val version = IntArray(2)
        EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

        val values = IntArray(1)
        EGL14.eglQueryContext(
            eglDisplay, eglContext, EGL14.EGL_CONTEXT_CLIENT_VERSION,
            values, 0
        )

        val surfaceAttribs = intArrayOf(
            EGL14.EGL_NONE
        )

        val eglSurface = EGL14.eglCreateWindowSurface(
            eglDisplay, eglConfig, surface,
            surfaceAttribs, 0
        )
        return eglSurface
    }

    /**
     * 设置surface为gles当前操作对象
     */
    fun makeCurrentSurface(): Boolean {
        return EGL14.eglMakeCurrent(
            eglDisplay,
            eglSurface/*draw Surface*/,
            eglSurface/*read Surface*/,
            eglContext
        )
    }

    fun clearSurfaceBuffer() {
        // 清空缓冲区
        // 设置清屏颜色
        GLES20.glClearColor(0f, 0f, 0f, 0f)

        //执行清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        //交换缓冲区
        EGL14.eglSwapBuffers(eglDisplay, eglSurface)
    }

    fun drawTexture(textureId: Int) {
        if (width != -1 && height != -1){
            GLES20.glViewport(0,0,width,height)
        }
        val render = TextureRender(textureId,width, height)
        render.render()
        EGL14.eglSwapBuffers(eglDisplay, eglSurface)
    }

    /**
     * 释放资源
     */
    fun release() {
        EGL14.eglDestroySurface(eglDisplay, eglSurface)
        EGL14.eglMakeCurrent(
            eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
            EGL14.EGL_NO_CONTEXT
        )
        EGL14.eglDestroyContext(eglDisplay, eglContext)
        EGL14.eglReleaseThread()
        EGL14.eglTerminate(eglDisplay)
    }
}