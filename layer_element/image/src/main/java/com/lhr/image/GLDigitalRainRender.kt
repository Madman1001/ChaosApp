package com.lhr.image

import android.content.Context
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.lhr.opengl.utils.GLESUtils
import com.lhr.opengl.utils.ShaderUtils
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
    private var mProgramHandle: Int = 0
    private val mRect: Rect = Rect()
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
            val vertexString= ShaderUtils.readShader(context, R.raw.default_v)
            val fragmentString= ShaderUtils.readShader(context, R.raw.digital_rain_f)
            mProgramHandle = GLESUtils.createProgram(vertexString, fragmentString)
        }

        //创建顶点坐标缓冲区
        if (mVertexBuffer == null){
            mVertexBuffer = GLESUtils.createFloatBuffer(VERTEX_DATA)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        gl?.glViewport(0, 0, width, height)
        mRect.set(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Select the program.
        GLES20.glUseProgram(mProgramHandle)
        GLESUtils.checkGlError("glUseProgram")

        /* ---------------------加载顶点坐标----------------------- */
        val mPositionLocation = GLES20.glGetAttribLocation(mProgramHandle, "aPos")
        mVertexBuffer?.position(0)
        GLES20.glVertexAttribPointer(
            mPositionLocation,          /* 目标地址 */
            2,                     /* 纹理坐标中每个点占的向量个数 */
            GLES20.GL_FLOAT,            /* 坐标数据类型 */
            false,            /* 是否归一化 */
            0,                    /* 数据间的间隔 */
            mVertexBuffer               /* 数据源 */
        )
        GLES20.glEnableVertexAttribArray(mPositionLocation)
        GLESUtils.checkGlError("mPositionLocation")

        /* ---------------------设置参数----------------------- */
        val uTime = GLES20.glGetUniformLocation(mProgramHandle, "time")
        GLES20.glUniform1f(uTime, System.currentTimeMillis().toFloat())
        val uResolution = GLES20.glGetUniformLocation(mProgramHandle, "resolution")
        GLES20.glUniform2f(uResolution, 1.0f,1.0f)

        GLES20.glDrawArrays(
            GLES20.GL_TRIANGLE_FAN,        /* 绘制模式 */
            0,                        /* 顶点起始位置 */
            VERTEX_DATA.size / 2     /* 数据长度 */
        )
        GLESUtils.checkGlError("glDrawArrays")

        // 解绑
        GLES20.glDisableVertexAttribArray(mPositionLocation)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)
    }
}