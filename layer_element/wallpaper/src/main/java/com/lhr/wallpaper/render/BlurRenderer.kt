package com.lhr.wallpaper.render

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import com.lhr.common.ext.readText
import com.lhr.wallpaper.R
import com.lhr.wallpaper.base.GLHelper
import com.lhr.wallpaper.base.GLRenderer
import com.lhr.wallpaper.base.GLSurface
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

/**
 * @CreateDate: 2022/12/14
 * @Author: mac
 * @Description: opengl图片模糊渲染
 */
class BlurRenderer(val context: Context, var bitmap: Bitmap): GLRenderer() {
    private val TAG = "TestRenderer"
    private var program = 0
    private var mTextureId = 0
    private val vertices: FloatBuffer by lazy {
        GLHelper.createFloatBuffer(
            floatArrayOf(
                -1f, -1f, 1f, 1f,
                -1f, 1f, 1f, 1f,
                1f, 1f, 1f, 1f,
                1f, -1f, 1f, 1f,
            )
        )
    }

    private val fragments: FloatBuffer by lazy {
        GLHelper.createFloatBuffer(
            floatArrayOf(
                0f, 1f,
                0f, 0f,
                1f, 0f,
                1f, 1f
            )
        )
    }

    // 顶点着色器的脚本
    private val verticesShader by lazy {
        context.resources.openRawResource(R.raw.guass_v).readText()
    }

    // 片元着色器的脚本
    private val fragmentShader by lazy {
        context.resources.openRawResource(R.raw.guass_f).readText()
    }

    override fun onCreated() {
        //基于顶点着色器与片元着色器创建程序
        program = GLHelper.createProgram(verticesShader, fragmentShader)
        // 获取着色器中的属性引用id(传入的字符串就是我们着色器脚本中的属性名)
        mTextureId = GLHelper.loadTexture(bitmap)
    }

    override fun onUpdate() {}

    override fun onDrawFrame(outputSurface: GLSurface?) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        // Select the program.
        GLES20.glUseProgram(program)
        GLHelper.checkGlError("glUseProgram")

        /* ---------------------加载纹理坐标----------------------- */
        val aTexCoordLocation = GLES20.glGetAttribLocation(program,"aCoordinate")
        fragments.position(0)
        GLES20.glVertexAttribPointer(
            aTexCoordLocation,          /* 目标地址 */
            2,                     /* 纹理坐标中每个点占的向量个数 */
            GLES20.GL_FLOAT,            /* 坐标数据类型 */
            false,            /* 是否归一化 */
            0,                    /* 数据间的间隔 */
            fragments                   /* 数据源 */
        )
        GLES20.glEnableVertexAttribArray(aTexCoordLocation)
        GLHelper.checkGlError("mTexVertexBuffer")

        /* ---------------------加载顶点坐标----------------------- */
        val mPositionLocation = GLES20.glGetAttribLocation(program, "aPos")
        vertices.position(0)
        GLES20.glVertexAttribPointer(
            mPositionLocation,          /* 目标地址 */
            4,                     /* 纹理坐标中每个点占的向量个数 */
            GLES20.GL_FLOAT,            /* 坐标数据类型 */
            false,            /* 是否归一化 */
            0,                    /* 数据间的间隔 */
            vertices                    /* 数据源 */
        )
        GLES20.glEnableVertexAttribArray(mPositionLocation)
        GLHelper.checkGlError("mVertexBuffer")

        /* ---------------------设置模糊配置----------------------- */
        val uBlurRadius = GLES20.glGetUniformLocation(program, "uBlurRadius")
        GLES20.glUniform1i(uBlurRadius, 5)
        val uBlurOffset = GLES20.glGetUniformLocation(program, "uBlurOffset")
        GLES20.glUniform2f(uBlurOffset, 1.0f/300.0f,1.0f/300.0f)

        /* ---------------------加载纹理----------------------- */
        val uSampler =  GLES20.glGetUniformLocation(program,"uSampler")
        GLES20.glUniform1i(uSampler, 0)
        // 开启纹理透明混合，这样才能绘制透明图片
        GLES20.glEnable(GL10.GL_BLEND)
        GLES20.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA)
        // 设置当前活动的纹理单元为纹理单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId)
        GLHelper.checkGlError("uSampler")

        GLES20.glDrawArrays(
            GLES20.GL_TRIANGLE_FAN,        /* 绘制模式 */
            0,                        /* 顶点起始位置 */
            4                        /* 数据长度 */
        )
        GLHelper.checkGlError("glDrawArrays")

        // 解绑
        GLES20.glDisableVertexAttribArray(mPositionLocation)
        GLES20.glDisableVertexAttribArray(aTexCoordLocation)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)
    }

    override fun onDestroy() {

    }
}