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


/**
 * @author lhr
 * @date 7/12/2022
 * @des opengl图片渲染
 */
class BitmapRenderer(val context: Context, var bitmap: Bitmap): GLRenderer() {
    private val TAG = "TestRenderer"
    private var program = 0
    private var vPosition = 0
    private var aTexCoord = 0
    private var uTextureUnit = 0
    private var mTextureId = 0

    private val vertices: FloatBuffer by lazy {
        GLHelper.createFloatBuffer(
            floatArrayOf(
                -1f, -1f,
                -1f, 1f,
                1f, 1f,
                1f, -1f
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
        context.resources.openRawResource(R.raw.image_v).readText()
    }

    // 片元着色器的脚本
    private val fragmentShader by lazy {
        context.resources.openRawResource(R.raw.image_f).readText()
    }

    override fun onCreated() {
        //基于顶点着色器与片元着色器创建程序
        program = GLHelper.createProgram(verticesShader, fragmentShader)
        // 获取着色器中的属性引用id(传入的字符串就是我们着色器脚本中的属性名)
        vPosition = GLES20.glGetAttribLocation(program, "vPosition")
        aTexCoord = GLES20.glGetAttribLocation(program, "aTexCoord")
        uTextureUnit = GLES20.glGetUniformLocation(program, "uTextureUnit")
        mTextureId = GLHelper.loadTexture(bitmap)
    }

    override fun onUpdate() {}

    override fun onDrawFrame(surface: GLSurface?) {
        // 设置clear color颜色RGBA(这里仅仅是设置清屏时GLES20.glClear()用的颜色值而不是执行清屏)
        GLES20.glClearColor(0.0f, 0f, 0f, 0.0f)

        // 清除深度缓冲与颜色缓冲(清屏,否则会出现绘制之外的区域花屏)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        // 使用某套shader程序
        GLES20.glUseProgram(program)
        vertices.position(0)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertices)
        // 为画笔指定顶点位置数据(vPosition)
        fragments.position(0)
        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0, fragments)
        // 允许顶点位置数据数组
        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glEnableVertexAttribArray(aTexCoord)

        GLES20.glUniform1i(uTextureUnit, 0)
        // 开启纹理透明混合，这样才能绘制透明图片
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        // 设置当前活动的纹理单元为纹理单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        // 将纹理ID绑定到当前活动的纹理单元上
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId)

        GLES20.glDrawArrays(
            GLES20.GL_TRIANGLE_FAN,        /* 绘制模式 */
            0,                        /* 顶点起始位置 */
            4     /* 数据长度 */
        )
        // 解绑
        GLES20.glDisableVertexAttribArray(vPosition)
        GLES20.glDisableVertexAttribArray(aTexCoord)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glUseProgram(0)
    }

    override fun onDestroy() {}
}