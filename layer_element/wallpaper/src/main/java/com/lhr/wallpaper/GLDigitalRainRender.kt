package com.lhr.wallpaper

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.lhr.common.ext.readText
import com.lhr.wallpaper.base.GLHelper
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @CreateDate: 2022/6/15
 * @Author: mac
 * @Description: 数字雨渲染
 */
class GLDigitalRainRender(private val context: Context)
    : GLSurfaceView.Renderer {
    private var createTime = 0L
    private var mProgramHandle: Int = 0
    private var uTime = 0
    private var uResolution = 0
    private var mPositionLocation = 0
    private var iChannel0 = 0
    private var iChannel1 = 0
    private var iTexture0 = 0
    private var iTexture1 = 0
    private var iChannelResolution = 0

    private var texture1Size = FloatArray(2)
    private val size = FloatArray(2)
    /**
     * 顶点坐标
     */
    private val VERTEX_DATA = floatArrayOf(
        -1f, -1f,
        -1f, 1f,
        1f, 1f,
        1f, -1f
    )
    private var mVertexBuffer: FloatBuffer? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        gl?.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        //创建着色器程序
        if (mProgramHandle == 0) {
            val vertexString = context.resources.openRawResource(R.raw.default_v).readText()
            val fragmentString= context.resources.openRawResource(R.raw.digital_rain_f).readText()
            mProgramHandle = GLHelper.createProgram(vertexString, fragmentString)
        }

        //创建顶点坐标缓冲区
        if (mVertexBuffer == null){
            mVertexBuffer = GLHelper.createFloatBuffer(VERTEX_DATA)
        }

        mPositionLocation = GLES20.glGetAttribLocation(mProgramHandle, "aPos")
        uTime = GLES20.glGetUniformLocation(mProgramHandle, "time")
        uResolution = GLES20.glGetUniformLocation(mProgramHandle, "resolution")
        iChannelResolution = GLES20.glGetUniformLocation(mProgramHandle, "iChannelResolution")
        iChannel0 =  GLES20.glGetUniformLocation(mProgramHandle,"iChannel0")
        iChannel1 =  GLES20.glGetUniformLocation(mProgramHandle,"iChannel1")

        createTime = System.currentTimeMillis()

        val bitmap0 =
            BitmapFactory.decodeResource(context.resources,R.raw.img)
        val bitmap1 =
            BitmapFactory.decodeResource(context.resources,R.raw.img_1)
        iTexture0 = GLHelper.loadTexture(bitmap0)
        iTexture1 = GLHelper.loadTexture(bitmap1)
        texture1Size[0] = bitmap1.width.toFloat()
        texture1Size[1] = bitmap1.height.toFloat()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        gl?.glViewport(0, 0, width, height)
        size[0] = width.toFloat()
        size[1] = height.toFloat()
    }

    override fun onDrawFrame(gl: GL10?) {
        val time = (System.currentTimeMillis() - createTime).toFloat() / 1000f
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        // Select the program.
        GLES20.glUseProgram(mProgramHandle)
        GLHelper.checkGlError("glUseProgram")

        /* ---------------------加载顶点坐标----------------------- */
        mVertexBuffer?.position(0)
        GLES20.glVertexAttribPointer(
            mPositionLocation,          /* 目标地址 */
            2,                     /* 纹理坐标中每个点占的向量个数 */
            GLES20.GL_FLOAT,            /* 坐标数据类型 */
            true,            /* 是否归一化 */
            0,                    /* 数据间的间隔 */
            mVertexBuffer               /* 数据源 */
        )
        GLES20.glEnableVertexAttribArray(mPositionLocation)
        GLHelper.checkGlError("mPositionLocation")

        /* ---------------------设置参数----------------------- */
        GLES20.glUniform1f(uTime, time)
        GLES20.glUniform2f(uResolution, size[0], size[1])
        GLES20.glUniform2f(iChannelResolution, texture1Size[0], texture1Size[1])

        // 开启纹理透明混合，这样才能绘制透明图片
        GLES20.glEnable(GL10.GL_BLEND)
        GLES20.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glUniform1i(iChannel0, 0)
        // 设置当前活动的纹理单元为纹理单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, iTexture0)
        GLES20.glUniform1i(iChannel1, 1)
        // 设置当前活动的纹理单元为纹理单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, iTexture1)

        GLES20.glDrawArrays(
            GLES20.GL_TRIANGLE_FAN,        /* 绘制模式 */
            0,                        /* 顶点起始位置 */
            4                        /* 数据长度 */
        )
        gl?.glFinish()
        GLHelper.checkGlError("glDrawArrays")

        // 解绑
        GLES20.glDisableVertexAttribArray(mPositionLocation)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)
    }
}