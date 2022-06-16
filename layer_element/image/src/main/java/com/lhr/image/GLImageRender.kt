package com.lhr.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.renderscript.Matrix4f
import com.lhr.opengl.utils.GLESUtils
import com.lhr.opengl.utils.ShaderUtils
import com.lhr.opengl.utils.TextureUtils
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @CreateDate: 2022/6/15
 * @Author: mac
 * @Description:
 */
class GLImageRender(private val context: Context,
                    private val bitmap: Bitmap)
    : GLSurfaceView.Renderer {
    private var mProgramHandle: Int = 0
    private var mTextureId: Int = 0
    private val mRect: Rect = Rect()

    private var mVertexBuffer: FloatBuffer? = null
    private var mTexVertexBuffer: FloatBuffer? = null

    /**
     * 顶点坐标
     */
    private val VERTEX_DATA = floatArrayOf(
        -1f, -1f,
        -1f, 1f,
        1f, 1f,
        1f, -1f
    )

    /**
     * 纹理坐标
     */
    private val TEX_VERTEX = floatArrayOf(
        0f, 1f,
        0f, 0f,
        1f, 0f,
        1f, 1f
    )
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        gl?.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        //创建着色器程序
        if (mProgramHandle == 0) {
            val vertexString= ShaderUtils.readShader(context, R.raw.image_v)
            val fragmentString= ShaderUtils.readShader(context, R.raw.image_f)
            mProgramHandle = GLESUtils.createProgram(vertexString, fragmentString)
        }
        //创建纹理贴图
        if (mTextureId == 0){
            mTextureId = TextureUtils.loadTexture(bitmap)
        }

        //创建顶点坐标缓冲区
        if (mVertexBuffer == null){
            mVertexBuffer = GLESUtils.createFloatBuffer(VERTEX_DATA)
        }

        //创建纹理坐标缓冲区
        if (mTexVertexBuffer == null){
            mTexVertexBuffer = GLESUtils.createFloatBuffer(TEX_VERTEX)
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

        /* ---------------------加载纹理坐标----------------------- */
        val aTexCoordLocation = GLES20.glGetAttribLocation(mProgramHandle,"a_TexCoord")
        mTexVertexBuffer?.position(0)
        GLES20.glVertexAttribPointer(
            aTexCoordLocation,          /* 目标地址 */
            2,                     /* 纹理坐标中每个点占的向量个数 */
            GLES20.GL_FLOAT,            /* 坐标数据类型 */
            false,            /* 是否归一化 */
            0,                    /* 数据间的间隔 */
            mTexVertexBuffer            /* 数据源 */
        )
        GLES20.glEnableVertexAttribArray(aTexCoordLocation)
        GLESUtils.checkGlError("mTexVertexBuffer")

        /* ---------------------加载顶点坐标----------------------- */
        val mPositionLocation = GLES20.glGetAttribLocation(mProgramHandle, "a_Position")
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
        GLESUtils.checkGlError("mVertexBuffer")

        /* ---------------------加载纹理----------------------- */
        val uTextureUnitLocation =  GLES20.glGetUniformLocation(mProgramHandle,"u_TextureUnit")
       GLES20.glUniform1i(uTextureUnitLocation, 0)
        // 开启纹理透明混合，这样才能绘制透明图片
        GLES20.glEnable(GL10.GL_BLEND)
        GLES20.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA)
        // 设置当前活动的纹理单元为纹理单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId)
        GLESUtils.checkGlError("uTextureUnitLocation")

        GLES20.glDrawArrays(
            GLES20.GL_TRIANGLE_FAN,        /* 绘制模式 */
            0,                        /* 顶点起始位置 */
            VERTEX_DATA.size / 2     /* 数据长度 */
        )
        GLESUtils.checkGlError("glDrawArrays")

        // 解绑
        GLES20.glDisableVertexAttribArray(mPositionLocation)
        GLES20.glDisableVertexAttribArray(aTexCoordLocation)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)
    }
}