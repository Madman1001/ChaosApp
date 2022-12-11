package com.lhr.learn.bitmap.gl

import android.opengl.*
import android.opengl.EGL14.EGL_NO_SURFACE
import android.util.Log
import androidx.annotation.NonNull
import java.util.concurrent.ArrayBlockingQueue


/**
 * @author lhr
 * @date 7/12/2022
 * @des
 */
abstract class GLRenderer: Thread() {
    private val TAG = "GLThread"
    private var eglConfig: EGLConfig? = null
    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT

    private var eventQueue: ArrayBlockingQueue<Event>
    private var outputSurfaces: MutableList<GLSurface?>
    private var rendering = false
    private var isRelease = false

    init {
        name = "GLRenderer-$id"
        outputSurfaces = ArrayList()
        rendering = false
        isRelease = false
        eventQueue = ArrayBlockingQueue(100)
    }

    private fun makeOutputSurface(surface: GLSurface?): Boolean {
        // 创建Surface缓存
        try {
            when (surface!!.type) {
                GLSurface.TYPE_WINDOW_SURFACE -> {
                    val attributes = intArrayOf(EGL14.EGL_NONE)
                    // 创建失败时返回EGL14.EGL_NO_SURFACE
                    surface.eglSurface = EGL14.eglCreateWindowSurface(
                        eglDisplay,
                        eglConfig,
                        surface.surface,
                        attributes,
                        0
                    )
                }
                GLSurface.TYPE_PBUFFER_SURFACE -> {
                    val attributes = intArrayOf(
                        EGL14.EGL_WIDTH, surface.viewport.width,
                        EGL14.EGL_HEIGHT, surface.viewport.height,
                        EGL14.EGL_NONE
                    )
                    // 创建失败时返回EGL14.EGL_NO_SURFACE
                    surface.eglSurface =
                        EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig, attributes, 0)
                }
                GLSurface.TYPE_PIXMAP_SURFACE -> {
                    Log.w(TAG, "nonsupport pixmap surface")
                    return false
                }
                else -> {
                    Log.w(TAG, "surface type error " + surface.type)
                    return false
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "can't create eglSurface")
            surface!!.eglSurface = EGL_NO_SURFACE
            return false
        }
        return true
    }

    fun addSurface(@NonNull surface: GLSurface?) {
        val event = Event(Event.ADD_SURFACE)
        event.param = surface
        if (!eventQueue.offer(event)) Log.e(TAG, "queue full")
    }

    fun removeSurface(@NonNull surface: GLSurface?) {
        val event = Event(Event.REMOVE_SURFACE)
        event.param = surface
        if (!eventQueue.offer(event)) Log.e(TAG, "queue full")
    }

    /**
     * 开始渲染
     * 启动线程并等待初始化完毕
     */
    fun startRender() {
        if (!eventQueue.offer(Event(Event.START_RENDER))) Log.e(TAG, "queue full")
        if (state == State.NEW) {
            super.start() // 启动渲染线程
        }
    }

    fun stopRender() {
        if (!eventQueue.offer(Event(Event.STOP_RENDER))) Log.e(TAG, "queue full")
    }

    fun postRunnable(@NonNull runnable: Runnable?): Boolean {
        val event = Event(Event.RUNNABLE)
        event.param = runnable
        if (!eventQueue.offer(event)) {
            Log.e(TAG, "queue full")
            return false
        }
        return true
    }

    override fun start() {
        Log.w(TAG, "Don't call this function")
    }

    fun requestRender() {
        eventQueue.offer(Event(Event.REQ_RENDER))
    }

    /**
     * 创建OpenGL环境
     */
    private fun createGL() {
        // 获取显示设备(默认的显示设备)
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        // 初始化
        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw RuntimeException("EGL error " + EGL14.eglGetError())
        }
        // 获取FrameBuffer格式和能力
        val configAttribs = intArrayOf(
            EGL14.EGL_BUFFER_SIZE, 32,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
            EGL14.EGL_SAMPLE_BUFFERS, EGL14.EGL_TRUE,
            EGL14.EGL_SAMPLES, 4,
            EGL14.EGL_NONE
        )
        val numConfigs = IntArray(1)
        val configs: Array<EGLConfig?> = arrayOfNulls<EGLConfig>(1)
        if (!EGL14.eglChooseConfig(
                eglDisplay,
                configAttribs,
                0,
                configs,
                0,
                configs.size,
                numConfigs,
                0
            )
        ) {
            throw RuntimeException("EGL error " + EGL14.eglGetError())
        }
        eglConfig = configs[0]
        // 创建OpenGL上下文(可以先不设置EGLSurface，但EGLContext必须创建，
        // 因为后面调用GLES方法基本都要依赖于EGLContext)
        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )
        eglContext =
            EGL14.eglCreateContext(eglDisplay, eglConfig, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
        if (eglContext === EGL14.EGL_NO_CONTEXT) {
            throw RuntimeException("EGL error " + EGL14.eglGetError())
        }
        // 设置默认的上下文环境和输出缓冲区(小米4上如果不设置有效的eglSurface后面创建着色器会失败，可以先创建一个默认的eglSurface)
        //EGL14.eglMakeCurrent(eglDisplay, surface.eglSurface, surface.eglSurface, eglContext);
        EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, eglContext)
    }

    /**
     * 销毁OpenGL环境
     */
    private fun destroyGL() {
        EGL14.eglDestroyContext(eglDisplay, eglContext)
        eglContext = EGL14.EGL_NO_CONTEXT
        eglDisplay = EGL14.EGL_NO_DISPLAY
    }

    /**
     * 渲染到各个eglSurface
     */
    private fun render() {
        // 渲染(绘制)
        for (output in outputSurfaces) {
            if (output!!.eglSurface === EGL_NO_SURFACE) {
                if (!makeOutputSurface(output)) continue
            }
            // 设置当前的上下文环境和输出缓冲区
            EGL14.eglMakeCurrent(eglDisplay, output!!.eglSurface, output.eglSurface, eglContext)
            // 设置视窗大小及位置
            GLES20.glViewport(
                output.viewport.x,
                output.viewport.y,
                output.viewport.width,
                output.viewport.height
            )
            // 绘制
            onDrawFrame(output)
            // 交换显存(将surface显存和显示器的显存交换)
            EGL14.eglSwapBuffers(eglDisplay, output.eglSurface)
        }
    }

    override fun run() {
        var event: Event
        Log.d(TAG, "$name: render create")
        createGL()
        onCreated()
        // 渲染
        while (!isRelease) {
            try {
                event = eventQueue.take()
                when (event.event) {
                    Event.ADD_SURFACE -> {

                        // 创建eglSurface
                        val surface = event.param as GLSurface?
                        Log.d(TAG, "add:$surface")
                        makeOutputSurface(surface)
                        outputSurfaces.add(surface)
                    }
                    Event.REMOVE_SURFACE -> {
                        val surface = event.param as GLSurface?
                        Log.d(TAG, "remove:$surface")
                        EGL14.eglDestroySurface(eglDisplay, surface!!.eglSurface)
                        outputSurfaces.remove(surface)
                    }
                    Event.START_RENDER -> rendering = true
                    Event.REQ_RENDER -> if (rendering) {
                        onUpdate()
                        render() // 如果surface缓存没有释放(被消费)那么这里将卡住
                    }
                    Event.STOP_RENDER -> rendering = false
                    Event.RUNNABLE -> (event.param as Runnable?)!!.run()
                    Event.RELEASE -> isRelease = true
                    else -> Log.e(TAG, "event error: $event")
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        // 回调
        onDestroy()
        // 销毁eglSurface
        for (outputSurface in outputSurfaces) {
            EGL14.eglDestroySurface(eglDisplay, outputSurface!!.eglSurface)
            outputSurface.eglSurface = EGL_NO_SURFACE
        }
        destroyGL()
        eventQueue.clear()
        Log.d(TAG, "$name: render release")
    }

    /**
     * 退出OpenGL渲染并释放资源
     * 这里先将渲染器释放(renderer)再退出looper，因为renderer里面可能持有这个looper的handler，
     * 先退出looper再释放renderer可能会报一些警告信息(sending message to a Handler on a dead thread)
     */
    fun release() {
        if (eventQueue.offer(Event(Event.RELEASE))) {
            // 等待线程结束，如果不等待，在快速开关的时候可能会导致资源竞争(如竞争摄像头)
            // 但这样操作可能会引起界面卡顿，择优取舍
            while (isAlive) {
                try {
                    this.join(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 当创建完基本的OpenGL环境后调用此方法，可以在这里初始化纹理之类的东西
     */
    abstract fun onCreated()

    /**
     * 在渲染之前调用，用于更新纹理数据。渲染一帧调用一次
     */
    abstract fun onUpdate()

    /**
     * 绘制渲染，每次绘制都会调用，一帧数据可能调用多次(不同是输出缓存)
     * @param outputSurface 输出缓存位置surface
     */
    abstract fun onDrawFrame(outputSurface: GLSurface?)

    /**
     * 当渲染器销毁前调用，用户回收释放资源
     */
    abstract fun onDestroy()


    private fun getEGLErrorString(): String? {
        return GLUtils.getEGLErrorString(EGL14.eglGetError())
    }

    private class Event internal constructor(val event: Int) {
        var param: Any? = null

        companion object {
            const val ADD_SURFACE = 1 // 添加输出的surface
            const val REMOVE_SURFACE = 2 // 移除输出的surface
            const val START_RENDER = 3 // 开始渲染
            const val REQ_RENDER = 4 // 请求渲染
            const val STOP_RENDER = 5 // 结束渲染
            const val RUNNABLE = 6 //
            const val RELEASE = 7 // 释放渲染器
        }
    }
}